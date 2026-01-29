package com.example.monoauction.bids.dto;

import com.example.monoauction.common.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatusUpdateMessage {
    private Long itemId;
    private String title;
    private ItemStatus oldStatus;
    private ItemStatus newStatus;
    private LocalDateTime timestamp;
    private String message;
}
