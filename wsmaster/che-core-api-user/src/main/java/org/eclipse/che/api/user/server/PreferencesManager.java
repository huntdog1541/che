package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.PreferenceDao;

import javax.inject.Singleton;
import java.util.Map;

// TODO  implement (note update is mergable)

@Singleton
public class PreferencesManager {

    private PreferenceDao preferenceDao = null;

    public void save(String userId, Map<String, String> preferences) throws ServerException {
        preferenceDao.setPreferences(userId, preferences);
    }

    public void update(String userId, Map<String, String> preferences) throws ServerException {

    }

    public Map<String, String> find(String userId) {
        return null;
    }

    public void remove(String id) {

    }
}
