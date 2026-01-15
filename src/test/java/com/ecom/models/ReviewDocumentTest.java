package com.ecom.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewDocumentTest {

    @Test
    public void testRoundTripWithObjectId() {
        ReviewDocument review = new ReviewDocument(1, 2, 5, "Great product!");
        review.setId(new ObjectId().toHexString());
        review.setImages(new String[]{"http://example.com/1.png"});
        Map<String, Object> meta = new HashMap<>();
        meta.put("verified", true);
        review.setMetadata(meta);
        Map<String, Integer> votes = new HashMap<>();
        votes.put("10", 1);
        review.setHelpfulVotes(votes);

        Document doc = review.toDocument();
        assertNotNull(doc);
        // ensure _id is ObjectId when valid
        Object idObj = doc.get("_id");
        assertTrue(idObj instanceof ObjectId);

        ReviewDocument from = ReviewDocument.fromDocument(doc);
        assertNotNull(from);
        assertEquals(review.getUserId(), from.getUserId());
        assertEquals(review.getProductId(), from.getProductId());
        assertEquals(review.getRating(), from.getRating());
        assertArrayEquals(review.getImages(), from.getImages());
        assertEquals(1, from.getHelpfulVotes().size());
    }

    @Test
    public void testFromDocumentWithNumericHelpfulVotesVariants() {
        Document doc = new Document();
        doc.append("userId", 3)
           .append("productId", 4)
           .append("rating", 4)
           .append("comment", "Nice")
           .append("helpfulVotes", new Document().append("11", 1L).append("12", 2.0).append("13", "-1"));

        ReviewDocument review = ReviewDocument.fromDocument(doc);
        assertEquals(3, review.getUserId());
        assertEquals(4, review.getProductId());
        assertEquals(4, review.getRating());
        assertEquals(3, review.getHelpfulVotes().size());
        assertEquals(1, review.getHelpfulVotes().get("11").intValue());
        assertEquals(2, review.getHelpfulVotes().get("12").intValue());
        assertEquals(-1, review.getHelpfulVotes().get("13").intValue());
    }
}
