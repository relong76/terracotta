package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContentCustomPlaceholder;
import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;
import edu.iu.terracotta.repository.messaging.MessageContentStandardPlaceholderRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentPlaceholderService;

@Service
public class MessageContentPlaceholderServiceImpl implements MessageContentPlaceholderService {

    @Autowired MessageContentStandardPlaceholderRepository messageContentStandardPlaceholderRepository;

    @Override
    public String process(Message message, List<MessageContentStandardPlaceholder> standardPlaceholders, LtiUserEntity ltiUserEntity) throws MessageContentStandardPlaceholderNotFoundException {
        String body = message.getContent().getBody();
        Document document = parseDocumentFromString(body);

        // handle standard placeholders
        body = standardPlaceholders(standardPlaceholders, document, ltiUserEntity);

        // re-parse with standard placeholders in document
        document = parseDocumentFromString(body);

        // handle custom placeholders
        body = customPlaceholders(document, message.getContent().getCustomPlaceholders());

        // re-parse with standard placeholders in document
        document = parseDocumentFromString(body);

        return document.body().html();
    }

    private String standardPlaceholders(List<MessageContentStandardPlaceholder> standardPlaceholders, Document document, LtiUserEntity ltiUserEntity) throws MessageContentStandardPlaceholderNotFoundException {
        if (ltiUserEntity == null) {
            return document.html();
        }

        Elements placeholderElements = document.getElementsByAttributeValueContaining("data-id", "standard_");

        if (placeholderElements.isEmpty()) {
            return document.html();
        }

        for (Element placeholderElement : placeholderElements) {
            String dataId = placeholderElement.attr("data-id");

            String[] dataIdSplit = StringUtils.split(dataId, '_');
            String placementUuid = dataIdSplit[1];

            MessageContentStandardPlaceholder standardPlaceholder = standardPlaceholders.stream()
                .filter(placeholder -> placeholder.getUuid().equals(UUID.fromString(placementUuid)))
                .findFirst()
                .orElseThrow(() -> new MessageContentStandardPlaceholderNotFoundException(String.format("No standard placement found with UUID: [%s]", placementUuid)));

            // replace the <span> placeholder tag with the text
            placeholderElement.replaceWith(
                new TextNode(
                    handleStandardPlacementKeyReplacement(standardPlaceholder, ltiUserEntity)
                )
            );
        }

        return document.html();
    }

    private String customPlaceholders(Document document, List<MessageContentCustomPlaceholder> messageContentCustomPlaceholders) {
        if (CollectionUtils.isEmpty(messageContentCustomPlaceholders)) {
            return document.html();
        }

        return document.html();
    }

    private String handleStandardPlacementKeyReplacement(MessageContentStandardPlaceholder messageContentStandardPlaceholder, LtiUserEntity ltiUserEntity) {
        switch (messageContentStandardPlaceholder.getKey()) {
            case EMAIL:
                return ltiUserEntity.getEmail();
            case FIRSTNAME:
                return StringUtils.split(ltiUserEntity.getDisplayName(), " ")[0]; // TODO
            case LASTNAME:
                String lastName = "";
                String[] lastNameSplit = StringUtils.split(ltiUserEntity.getDisplayName(), " "); // TODO

                for (int i = 1; i < lastNameSplit.length; i++) {
                    lastName += " " + lastNameSplit[i];
                }

                return lastName;
            default:
                return "";
        }
    }

    private Document parseDocumentFromString(String body) {
        Document document = Jsoup.parse(body);
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.prettyPrint(false);
        document.outputSettings(outputSettings);

        return document;
    }

}
