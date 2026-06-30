package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.Feedback;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SocialLink entity.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}
