package org.hisp.dhis.expression;

@FunctionalInterface
public interface Data {

    Object value(UID uid0, UID... uid1n);

}
