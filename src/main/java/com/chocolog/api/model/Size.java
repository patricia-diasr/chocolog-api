package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sizes")
@SQLDelete(sql = "UPDATE sizes SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class Size implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @OneToMany(mappedBy = "size")
    @JsonManagedReference("size-orderitems")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "size")
    @JsonManagedReference("size-prices")
    private List<ProductPrice> productPrices = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "size")
    @JsonManagedReference("size-stocks")
    private List<Stock> stocks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "size")
    @JsonManagedReference("size-stockrecords")
    private List<StockRecord> stockRecords = new ArrayList<>();

    @Override
    public Long getId() {
        return this.id;
    }
}
