//@formatter:off
/**
 * $$Id$$
 * . * .
 * * RRRR  *    Copyright (c) 2016 EUIPO: European Union Intellectual
 * .   RR  R   .  Property Office (trade marks and designs)
 * *   RRR     *
 * .  RR RR  .   ALL RIGHTS RESERVED
 * * . _ . *
 */
//@formatter:on
package de.pandigo.security;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService {

    UserDetails loadUserByUsername(java.lang.String s)
            throws UsernameNotFoundException;

}