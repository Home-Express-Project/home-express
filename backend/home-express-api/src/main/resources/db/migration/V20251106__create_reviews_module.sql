-- Reviews module schema (bidirectional reviews, moderation, analytics)

CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    reviewer_type ENUM('CUSTOMER', 'TRANSPORT') NOT NULL,
    overall_rating DECIMAL(2,1) NOT NULL,
    punctuality_rating DECIMAL(2,1) DEFAULT NULL,
    professionalism_rating DECIMAL(2,1) DEFAULT NULL,
    communication_rating DECIMAL(2,1) DEFAULT NULL,
    care_rating DECIMAL(2,1) DEFAULT NULL,
    title VARCHAR(200) DEFAULT NULL,
    comment TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED') DEFAULT 'PENDING',
    is_verified BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    helpful_count INT DEFAULT 0,
    unhelpful_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    moderated_at DATETIME DEFAULT NULL,
    moderated_by BIGINT DEFAULT NULL,
    PRIMARY KEY (review_id),
    UNIQUE KEY uk_reviews_booking_type (booking_id, reviewer_type),
    KEY idx_reviews_reviewee_status_created (reviewee_id, status, created_at DESC),
    KEY idx_reviews_booking_side (booking_id, reviewer_type),
    KEY idx_reviews_reviewer_created (reviewer_id, created_at DESC),
    KEY idx_reviews_status_created (status, created_at DESC),
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
    CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(user_id),
    CONSTRAINT fk_reviews_moderator FOREIGN KEY (moderated_by) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_reviews_ratings CHECK (
        overall_rating BETWEEN 1.0 AND 5.0 AND
        (punctuality_rating IS NULL OR punctuality_rating BETWEEN 1.0 AND 5.0) AND
        (professionalism_rating IS NULL OR professionalism_rating BETWEEN 1.0 AND 5.0) AND
        (communication_rating IS NULL OR communication_rating BETWEEN 1.0 AND 5.0) AND
        (care_rating IS NULL OR care_rating BETWEEN 1.0 AND 5.0)
    ),
    CONSTRAINT chk_reviews_comment_length CHECK (CHAR_LENGTH(comment) BETWEEN 10 AND 5000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Photos attached to reviews
CREATE TABLE IF NOT EXISTS review_photos (
    photo_id BIGINT NOT NULL AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    photo_url TEXT NOT NULL,
    caption VARCHAR(200) DEFAULT NULL,
    display_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (photo_id),
    KEY idx_review_photos_review (review_id),
    CONSTRAINT fk_review_photos_review FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Transport/customer responses to reviews
CREATE TABLE IF NOT EXISTS review_responses (
    response_id BIGINT NOT NULL AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    responder_id BIGINT NOT NULL,
    response_text TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (response_id),
    UNIQUE KEY uk_review_responses (review_id),
    CONSTRAINT fk_review_responses_review FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_responses_responder FOREIGN KEY (responder_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Helpful / not helpful votes
CREATE TABLE IF NOT EXISTS review_helpfulness (
    id BIGINT NOT NULL AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    voter_id BIGINT NOT NULL,
    is_helpful BOOLEAN NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_helpfulness (review_id, voter_id),
    KEY idx_review_helpfulness_review (review_id),
    KEY idx_review_helpfulness_voter (voter_id),
    CONSTRAINT fk_review_helpfulness_review FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_helpfulness_voter FOREIGN KEY (voter_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Review reports for moderation workflow
CREATE TABLE IF NOT EXISTS review_reports (
    report_id BIGINT NOT NULL AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason ENUM('SPAM', 'INAPPROPRIATE', 'FAKE', 'OFFENSIVE', 'OTHER') NOT NULL,
    description TEXT DEFAULT NULL,
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED') DEFAULT 'PENDING',
    admin_notes TEXT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME DEFAULT NULL,
    resolved_by BIGINT DEFAULT NULL,
    PRIMARY KEY (report_id),
    UNIQUE KEY uk_review_reports (review_id, reporter_id),
    KEY idx_review_reports_review (review_id),
    KEY idx_review_reports_status_created (status, created_at DESC),
    CONSTRAINT fk_review_reports_review FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    CONSTRAINT fk_review_reports_resolver FOREIGN KEY (resolved_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
