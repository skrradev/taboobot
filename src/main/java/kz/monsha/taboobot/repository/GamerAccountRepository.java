package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GamerAccount;

public interface GamerAccountRepository {
    GamerAccount getByUserId(long userId);

    void save(GamerAccount gamerAccount);
}
