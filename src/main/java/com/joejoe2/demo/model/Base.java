package com.joejoe2.demo.model;

import com.joejoe2.demo.model.generator.UUIDv7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
@Data
public class Base {
  @Id
  @GeneratedValue(generator = "UUIDv7")
  @GenericGenerator(name = "UUIDv7", type = UUIDv7Generator.class)
  @Column(unique = true, updatable = false, nullable = false)
  protected UUID id;
}
