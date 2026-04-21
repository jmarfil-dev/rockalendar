package com.jmarfildev.rockalendar.notifications.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.CommonValidations;
import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;
import com.jmarfildev.rockalendar.notifications.api.dto.UnreadCountDto;
import com.jmarfildev.rockalendar.notifications.api.mapper.NotificationMapper;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.notifications.persistence.NotificationRepository;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    public Page<NotificationDto> list(UUID recipientId, List<NotificationType> types, Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        if (types != null && !types.isEmpty()) {
            return repository.findByRecipientIdAndTypeInOrderByCreatedAtDescIdDesc(recipientId, types, fixed).map(mapper::toDto);
        }
        return repository.findByRecipientIdOrderByCreatedAtDescIdDesc(recipientId, fixed).map(mapper::toDto);
    }

    public UnreadCountDto countUnread(UUID recipientId) {
        long user = repository.countByRecipientIdAndTypeInAndIsReadFalse(recipientId,
                                                                         NotificationType.ofBandeja(NotificationType.Bandeja.USER));
        long moderation =
                repository.countByRecipientIdAndTypeInAndIsReadFalse(recipientId,
                                                                     NotificationType.ofBandeja(NotificationType.Bandeja.MODERATION));
        long admin = repository.countByRecipientIdAndTypeInAndIsReadFalse(recipientId,
                                                                          NotificationType.ofBandeja(NotificationType.Bandeja.ADMIN));
        return new UnreadCountDto(user, moderation, admin);
    }
}
