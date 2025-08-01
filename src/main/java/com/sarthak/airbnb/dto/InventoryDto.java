package com.sarthak.airbnb.dto;

import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Room;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryDto {
    private Long id;
    private LocalDate date;
    private Integer bookedCount;
    private Integer reservedCount;
    private Integer totalCount;
    private BigDecimal surgeFactor;
    private BigDecimal price; //basePrice * surgeFactor;
    private Boolean closed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
