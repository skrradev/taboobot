package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GamerAccount;

public interface GamerAccountRepository {
    GamerAccount getByUserId(long userId);//TODO make  it return optional

    void save(GamerAccount gamerAccount);
}
