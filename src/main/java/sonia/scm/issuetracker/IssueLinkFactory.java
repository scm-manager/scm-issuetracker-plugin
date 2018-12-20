package sonia.scm.issuetracker;

public interface IssueLinkFactory {

    /**
     * Get the link to the Issue using the given <code>key</code>
     *
     * @param key
     * @return
     */
    String createLink(String key);
}
