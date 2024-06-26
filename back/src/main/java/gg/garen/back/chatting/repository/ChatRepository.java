package gg.garen.back.chatting.repository;

import gg.garen.back.chatting.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {
    Page<Chat> findAll(Pageable pageable);
}
