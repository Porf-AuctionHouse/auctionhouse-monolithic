package com.example.monoauction.notifications.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.user.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from:noreply@auctionhouse.com}")
    private String fromEmail;

    @Value("${app.name:Auction House}")
    private String appName;

    public void sendSimpleEmail(String to, String subject, String body){
        try{

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to: {}", to);

        }catch(Exception e){
            log.error("Failed to send email to: {}", to, e);
        }
    }

   private void sendTemplateEmail(String to, String subject, String templateName, Context context) {
        try {
            context.setVariable("appName", appName);
            context.setVariable("emailSubtitle", "Your Weekly Auction Platform");

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Template email sent to: {} using template: {}", to, templateName);

        } catch (Exception e) {
            log.error("Failed to send template email to: {}", to, e);
        }
   }


    public void sendItemSubmittedEmail(User seller, AuctionItem item) {
        String subject = appName + " - Item Submitted Successfully";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your item '%s' has been successfully submitted for review.\n\n" +
                        "Item Details:\n" +
                        "- Title: %s\n" +
                        "- Category: %s\n" +
                        "- Starting Price: $%.2f\n" +
                        "- Status: Under Review\n\n" +
                        "Our admin team will review your item within 1-2 business days.\n" +
                        "You will receive an email once the review is complete.\n\n" +
                        "Thank you for using %s!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                seller.getFullName(),
                item.getTitle(),
                item.getTitle(),
                item.getCategory(),
                item.getStartingPrice(),
                appName,
                appName
        );
        sendSimpleEmail(seller.getEmail(), subject, body);
    }

    public void sendItemApprovedEmail(User seller, AuctionItem item, AuctionBatch batch){
        String subject = appName + " - Item Approved Successfully";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Great news! Your item '%s' has been approved for auction.\n\n" +
                        "Auction Details:\n" +
                        "- Item: %s\n" +
                        "- Starting Price: $%.2f\n" +
                        "- Auction Start: %s\n" +
                        "- Auction End: %s\n\n" +
                        "Your item will go live on %s at %s.\n" +
                        "We'll notify you when bidding starts and when you receive bids.\n\n" +
                        "Good luck with your auction!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                seller.getFullName(),
                item.getTitle(),
                item.getTitle(),
                item.getStartingPrice(),
                batch.getAuctionStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                batch.getAuctionEndTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                batch.getAuctionStartTime().format(DateTimeFormatter.ofPattern("EEEE")),
                batch.getAuctionStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                appName
        );

        sendSimpleEmail(seller.getEmail(), subject, body);
    }

    public void sendItemRejectedEmail(User seller, AuctionItem item, String reason) {
        String subject = appName + " - Item Not Approved";

        String body = String.format(
                "Dear %s,\n\n" +
                        "Thank you for submitting '%s' to our auction platform.\n\n" +
                        "Unfortunately, we cannot approve this item at this time.\n\n" +
                        "Reason: %s\n\n" +
                        "You can resubmit the item with improvements during the next submission window " +
                        "(Monday-Wednesday).\n\n" +
                        "If you have any questions, please contact our support team.\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                seller.getFullName(),
                item.getTitle(),
                reason,
                appName
        );

        sendSimpleEmail(seller.getEmail(), subject, body);
    }

    public void sendNewBidEmail(User seller, AuctionItem item, Bid bid) {
        String subject = appName + " - New Bid on Your Item!";

        String body = String.format(
                "Dear %s,\n\n" +
                        "Good news! Someone just placed a bid on your item.\n\n" +
                        "Item: %s\n" +
                        "New Bid Amount: $%.2f\n" +
                        "Total Bids: %d\n\n" +
                        "You can track all bids in real-time on the auction page.\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                seller.getFullName(),
                item.getTitle(),
                bid.getAmount(),
                item.getTotalBids(),
                appName
        );

        sendSimpleEmail(seller.getEmail(), subject, body);
    }

    public void sendOutbidEmail(User bidder, AuctionItem item, BigDecimal newBidAmount) {
        String subject = appName + " - You've Been Outbid!";

        String body = String.format(
                "Dear %s,\n\n" +
                        "Someone just placed a higher bid on an item you're interested in.\n\n" +
                        "Item: %s\n" +
                        "New Highest Bid: $%.2f\n" +
                        "Your Last Bid: $%.2f\n\n" +
                        "Don't miss out! Place a new bid to stay in the running.\n\n" +
                        "The auction ends soon. Act fast!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                bidder.getFullName(),
                item.getTitle(),
                newBidAmount,
                item.getCurrentBid().subtract(item.getBidIncrement()), // Approximate previous bid
                appName
        );

        sendSimpleEmail(bidder.getEmail(), subject, body);
    }

    public void sendAuctionWonEmail(User winner, AuctionItem item) {
        String subject = appName + " - Congratulations! You Won!";

        String body = String.format(
                "Dear %s,\n\n" +
                        "Congratulations! You won the auction!\n\n" +
                        "Item: %s\n" +
                        "Your Winning Bid: $%.2f\n\n" +
                        "Next Steps:\n" +
                        "1. Complete payment within 48 hours\n" +
                        "2. Coordinate with the seller for delivery\n" +
                        "3. Leave feedback after receiving the item\n\n" +
                        "Payment Details:\n" +
                        "Amount Due: $%.2f\n\n" +
                        "Thank you for participating in our auction!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                winner.getFullName(),
                item.getTitle(),
                item.getCurrentBid(),
                item.getCurrentBid(),
                appName
        );

        sendSimpleEmail(winner.getEmail(), subject, body);
    }

    public void sendAuctionLostEmail(User bidder, AuctionItem item) {
        String subject = appName + " - Auction Ended";

        String body = String.format(
                "Dear %s,\n\n" +
                        "The auction for '%s' has ended.\n\n" +
                        "Unfortunately, you didn't win this time.\n" +
                        "Final Winning Bid: $%.2f\n\n" +
                        "Don't worry! New auctions start every week.\n" +
                        "Check out our current listings for more great items.\n\n" +
                        "Thank you for participating!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                bidder.getFullName(),
                item.getTitle(),
                item.getCurrentBid(),
                appName
        );

        sendSimpleEmail(bidder.getEmail(), subject, body);
    }

    public void sendAuctionStartedEmail(User user, AuctionBatch batch) {
        String subject = appName + " - Auction is Now LIVE!";

        String body = String.format(
                "Dear %s,\n\n" +
                        "The weekly auction has started!\n\n" +
                        "Auction Details:\n" +
                        "- Batch: %s\n" +
                        "- Started: %s\n" +
                        "- Ends: %s\n" +
                        "- Live Items: %d\n\n" +
                        "Browse all items and start bidding now!\n" +
                        "Don't miss out on great deals.\n\n" +
                        "Happy bidding!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                user.getFullName(),
                batch.getBatchCode(),
                batch.getAuctionStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                batch.getAuctionEndTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                batch.getTotalItemsApproved(),
                appName
        );

        sendSimpleEmail(user.getEmail(), subject, body);
    }

    public void sendAuctionEndingSoonEmail(User user, AuctionBatch batch) {
        String subject = appName + " - Auction Ending in 1 Hour!";

        String body = String.format(
                "Dear %s,\n\n" +
                        "FINAL CALL! The auction ends in 1 hour.\n\n" +
                        "This is your last chance to:\n" +
                        "- Place final bids\n" +
                        "- Check your watched items\n" +
                        "- Make sure you're the highest bidder\n\n" +
                        "The auction ends at %s.\n\n" +
                        "Don't miss out!\n\n" +
                        "Best regards,\n" +
                        "The %s Team",
                user.getFullName(),
                batch.getAuctionEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                appName
        );

        sendSimpleEmail(user.getEmail(), subject, body);
    }
}
