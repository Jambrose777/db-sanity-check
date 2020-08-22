package org.worldcubeassociation.dbsanitycheck.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

	@Value("${service.mail.send}")
	private boolean sendMail;

	@Value("${service.mail.to}")
	private String emailTo;

	@Value("${service.mail.from}")
	private String mailFrom;

	@Value("${service.mail.subject}")
	private String subject;

	@Autowired
	private JavaMailSender emailSender;

	@Override
	public void sendEmail(List<AnalysisBean> analysisResult) throws MessagingException {
		if (sendMail) {
			log.info("Sending email with the analysis");

			MimeMessage message = emailSender.createMimeMessage();

			boolean multipart = true;
			MimeMessageHelper helper = new MimeMessageHelper(message, multipart);

			helper.setFrom(mailFrom);
			helper.setTo(emailTo);
			helper.setSubject(subject);

			boolean html = true;
			helper.setText(getText(analysisResult), html);

			// Email the log file
			FileSystemResource file = new FileSystemResource(new File("log/db-sanity-check.log"));
			helper.addAttachment("db-sanity-check.txt", file);

			emailSender.send(message);
		} else {
			log.info("Not sending email");
		}

	}

	private String getText(List<AnalysisBean> analysisResult) {
		StringBuilder sb = new StringBuilder("<h2>Sanity Check Results</h2>\n\n");

		if (analysisResult.size() == 0) {
			sb.append("<p>No results to show</p>\n");
		} else {
			sb.append("<p>Found inconsistencies in ").append(analysisResult.size()).append(" topics.</p>\n\n");
		}

		for (AnalysisBean analysis : analysisResult) {
			sb.append(String.format("<h3>[%s] %s</h3>%n", analysis.getCategory(), analysis.getTopic()));
			sb.append("<div style=\"overflow-x: auto;\">\n");
			sb.append(" <table style=\"border: 1px solid black;\">\n");
			sb.append("  <thead>\n");
			sb.append("   <tr style=\"background-color: #f2f2f2;\">");
			for (String header : analysis.getKeys()) {
				sb.append("<th scope=\"col\" style=\"border: 1px solid black;\">").append(header).append("</th>");
			}
			sb.append("\n   </tr>\n");
			sb.append("  </thead>\n");
			sb.append("  <tbody>\n");
			for (Map<String, String> item : analysis.getAnalysis()) {
				sb.append("   <tr>\n");
				for (Entry<String, String> entry : item.entrySet()) {
					sb.append("    <td style=\"border: 1px solid black;\">").append(entry.getValue()).append("</td>\n");
				}
				sb.append("   </tr>\n");
			}
			sb.append("  </tbody>\n");
			sb.append(" </table>\n");
			sb.append("</div>\n");
			sb.append("<br>\n\n");
		}

		return sb.toString();
	}

}
