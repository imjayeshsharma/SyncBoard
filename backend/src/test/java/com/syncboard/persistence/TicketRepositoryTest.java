// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence;

import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;
import com.syncboard.persistence.entity.TicketEntity;
import com.syncboard.persistence.entity.UserEntity;
import com.syncboard.persistence.repository.TicketRepository;
import com.syncboard.persistence.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code @DataJpaTest} against a real {@code postgres:18-alpine} container (via
 * {@code @ServiceConnection}), so Flyway runs the real {@code db/migration} scripts and
 * {@code spring.jpa.hibernate.ddl-auto=validate} (application.yml) validates our entity mappings
 * against that exact schema — the same combination production runs with.
 *
 * <p>Only the round-trip test is implemented for this stage; the remaining cases are stubbed as
 * {@code @Disabled} so their intent is on record for stage 3 follow-up.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceConfig.class)
@Testcontainers
class TicketRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savedTicketRoundTripsStatusAsItsWireValue() {
        UserEntity reporter = userRepository.saveAndFlush(
                new UserEntity("dev-google-subject-test-reporter", "reporter@example.com", "Reporter One", "Support"));

        TicketEntity ticket = new TicketEntity();
        ticket.setTitle("Printer on fire");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(Priority.HIGH);
        ticket.setReporter(reporter);

        TicketEntity saved = ticketRepository.saveAndFlush(ticket);
        entityManager.clear();

        TicketEntity reloaded = ticketRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(reloaded.getPriority()).isEqualTo(Priority.HIGH);

        // Bypass JPA to confirm the *raw column* holds the wire value, not an ordinal or a name()
        // that happens to differ from the wire value (e.g. IN_PROGRESS vs. "InProgress").
        Object rawStatus = entityManager.getEntityManager()
                .createNativeQuery("select status from tickets where id = ?1")
                .setParameter(1, saved.getId())
                .getSingleResult();
        assertThat(rawStatus).isEqualTo("Open");
    }

    @Test
    @Disabled("TODO stage 3")
    void findAllByStatusInReturnsOnlyMatchingTickets() {
    }

    @Test
    @Disabled("TODO stage 3")
    void findAllByAssigneeIdReturnsOnlyAssignedTickets() {
    }

    @Test
    @Disabled("TODO stage 3")
    void findAllWithReporterAndAssigneeAvoidsNPlusOneLazyLoads() {
    }

    @Test
    @Disabled("TODO stage 3")
    void findBoardRowsReportsAccurateCommentAndLinkCounts() {
    }

    @Test
    @Disabled("TODO stage 3")
    void deletingTicketCascadesToLinksButLeavesHistoryOrphanedByDesign() {
        // ticket_links has ON DELETE CASCADE + orphanRemoval; ticket_status_history also has
        // ON DELETE CASCADE at the DB level (it's keyed to the ticket) but the entity association
        // carries no cascade/orphanRemoval, since history rows are never programmatically deleted.
    }

    @Test
    @Disabled("TODO stage 3")
    void staleVersionOnConcurrentUpdateThrowsOptimisticLockException() {
    }

    @Test
    @Disabled("TODO stage 3")
    void ticketStatusHistoryRepositoryExposesNoMutationMethods() {
    }
}
