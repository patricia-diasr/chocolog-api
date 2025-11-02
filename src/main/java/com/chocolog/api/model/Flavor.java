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
@Table(name = "flavors")
@SQLDelete(sql = "UPDATE flavors SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class Flavor implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @OneToMany(mappedBy = "flavor1")
    @JsonManagedReference("flavor1-orderitem")
    private List<OrderItem> flavor1Orders = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "flavor2")
    @JsonManagedReference("flavor2-orderitem")
    private List<OrderItem> flavor2Orders = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "flavor")
    @JsonManagedReference("flavor-prices")
    private List<ProductPrice> productPrices = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "flavor")
    @JsonManagedReference("flavor-stocks")
    private List<Stock> stocks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "flavor")
    @JsonManagedReference("flavor-stockrecords")
    private List<StockRecord> stockRecords = new ArrayList<>();

    @Override
    public Long getId() {
        return this.id;
    }
}
