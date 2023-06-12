package com.joejoe2.demo.model.generator;

import com.fasterxml.uuid.Generators;
import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class UUIDv7Generator implements IdentifierGenerator {
  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {
    return Generators.timeBasedEpochGenerator().generate();
  }
}
