package com.joejoe2.demo.model;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
@Data
public class Base {
  @Id
  @GeneratedValue(generator = "UUIDv7")
  @GenericGenerator(name = "UUIDv7", strategy = "com.joejoe2.demo.model.generator.UUIDv7Generator")
  @Column(unique = true, updatable = false, nullable = false)
  protected UUID id;
}
