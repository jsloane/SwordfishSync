package swordfishsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import swordfishsync.domain.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

}
