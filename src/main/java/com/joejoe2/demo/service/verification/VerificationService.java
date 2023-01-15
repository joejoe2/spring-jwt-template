package com.joejoe2.demo.service.verification;

import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.exception.InvalidOperation;

public interface VerificationService {
    /**
     * issue verification code related to the param email and send it out. note that the verification code
     * has an expiration time.
     *
     * @param email the email address related to the verification code
     * @return an object containing verification key and verification code
     */
    VerificationPair issueVerificationCode(String email);

    /**
     * try to verify the verification code with the verification key and email.
     * one must pass key,email, and code to check whether an VerificationCode object
     * is in db and deleted it if existed and not expired
     *
     * @param key   verification key
     * @param email related email
     * @param code  verification code
     * @throws InvalidOperation if there is no matching VerificationCode object in db, or it has been already expired
     */
    void verify(String key, String email, String code) throws InvalidOperation;

    /**
     * delete all expired verification codes
     */
    void deleteExpiredVerificationCodes();

    /**
     * delete all expired verify tokens
     */
    void deleteExpiredVerifyTokens();
}
