package org.bbop.termgenie.user.go;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.bbop.termgenie.tools.GitYaml;
import org.bbop.termgenie.tools.Md5Tool;
import org.bbop.termgenie.tools.YamlTool;
import org.bbop.termgenie.user.OrcidUserData;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.XrefUserData;
import org.bbop.termgenie.user.simple.SimpleUserDataProvider;

import java.io.File;
import java.util.*;

@Singleton
public class GoYamlUserDataProvider extends SimpleUserDataProvider {

    private static final Logger logger = Logger.getLogger(GoYamlUserDataProvider.class);

    private final GitYaml gitYamlFile;

    @Inject
    GoYamlUserDataProvider(GitYaml gitYamlFile) {
        super();
        this.gitYamlFile = gitYamlFile;
        File yamlFile = gitYamlFile.getYamlFile();
        if (!yamlFile.isFile() || !yamlFile.canRead()) {
            throw new RuntimeException("Invalid permissions file: " + yamlFile);
        }
    }

    static class Md5UserData extends UserData {

        private List<String> md5s = null;

        /**
         * @return the md5s
         */
        public List<String> getMd5s() {
            return md5s;
        }

        /**
         * @param md5s the md5s to set
         */
        public void setMd5s(List<String> md5s) {
            this.md5s = md5s;
        }
    }

    static List<UserData> loadRawUserData(File yamlFile) {
        List<Map> userEntries = (List<Map>) YamlTool.load(yamlFile);
        if (userEntries != null && userEntries.isEmpty() == false) {
            List<UserData> userDataList = new ArrayList<UserData>(userEntries.size());
            for (Map userEntry : userEntries) {
                UserData userData = new UserData();
                userData.setScreenname(getStringValue("nickname", userEntry));
                userData.setOrcid(getStringValue("uri", userEntry));
                userData.setXref(getStringValue("xref", userEntry));
                userData.setGuid(getStringValue("github", userEntry));
                userDataList.add(userData);
            }
            return userDataList;
        }

        return Collections.emptyList();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static List<Md5UserData> loadUserData(File yamlFile) {
        List<Map> userEntries = (List<Map>) YamlTool.load(yamlFile);
        if (userEntries != null && userEntries.isEmpty() == false) {
            List<Md5UserData> userDataList = new ArrayList<Md5UserData>(userEntries.size());
            for (Map userEntry : userEntries) {
                Md5UserData userData = new Md5UserData();
                userData.setScreenname(getStringValue("nickname", userEntry));
                userData.setOrcid(getStringValue("uri", userEntry));
                userData.setXref(getStringValue("xref", userEntry));
                userData.setMd5s(getStringList("email-md5", userEntry));
                userDataList.add(userData);
            }
            return userDataList;
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    private static String getStringValue(String key, Map map) {
        Object value = map.get(key);
        if (value != null && value instanceof CharSequence) {
            return value.toString();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> getStringList(String key, Map map) {
        Object value = map.get(key);
        if (value != null && value instanceof List) {
            List<String> result = new ArrayList<String>();
            for (Object obj : (List) value) {
                if (obj instanceof CharSequence) {
                    result.add(obj.toString());
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public UserData getUserDataPerGithubLogin(final String login) {
        List<UserData> data = loadRawUserData(gitYamlFile.getYamlFile());
        for (UserData userData : data) {
            String guid = userData.getGuid();
            if (guid != null && guid.equals(login)) {
                return userData;
            }
        }
        logger.warn("Could not retrieve user data for github login: " + login);
        return super.getUserDataPerGithubLogin(login);
    }

    @Override
    public UserData getUserDataPerEMail(final String email) {
        List<Md5UserData> data = loadUserData(gitYamlFile.getYamlFile());
        final String emailMd5 = Md5Tool.md5(email);
        for (Md5UserData userData : data) {
            List<String> md5s = userData.getMd5s();
            if (md5s != null) {
                for (String md5 : md5s) {
                    if (emailMd5.equalsIgnoreCase(md5)) {
                        userData.setEmail(email);
                        userData.setGuid(email);
                        if (userData.getScreenname() == null) {
                            userData.setScreenname(getNameFromEMail(email));
                        }
                        if (userData.getScmAlias() == null) {
                            userData.setScmAlias(extractSCMAlias(userData.getXref(), email));
                        }
                        return userData;
                    }
                }
            }
        }
        logger.warn("Could not retrieve user data for email: " + email);
        return super.getUserDataPerEMail(email);
    }

    @Override
    public List<XrefUserData> getXrefUserData() {
        List<Md5UserData> data = loadUserData(gitYamlFile.getYamlFile());
        List<XrefUserData> filtered = new ArrayList<XrefUserData>(data.size());
        for (UserData elem : data) {
            if (elem.getXref() != null) {
                filtered.add(elem);
            }
        }
        return filtered;
    }

    @Override
    public Set<String> getAdditionalXrefs() {
        return null;
    }

    @Override
    public List<OrcidUserData> getOrcIdUserData() {
        List<Md5UserData> data = loadUserData(gitYamlFile.getYamlFile());
        List<OrcidUserData> filtered = new ArrayList<OrcidUserData>(data.size());
        for (UserData elem : data) {
            if (elem.getOrcid() != null) {
                filtered.add(elem);
            }
        }
        return filtered;
    }
}
