import groovy.transform.Field
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.atlassian.jira.util.json.JSONArray
import com.atlassian.jira.util.json.JSONObject
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.component.ComponentAccessor

@Field final String OVERDUE_STYLE = """
<style>
    .my-notes-icon {
        position: relative;
        display: inline-block;
    }

    .my-notes-icon::after {
        content: "";
        position: absolute;
        top: 1px;
        right: -1px;
        width: 8px;
        height: 8px;
        background: #FF0000;
        border-radius: 50%;
    }
</style>
"""
@Field final String MYNOTES_PROPERTY_KEY = "com.troshin.jira.plugins.mynotes"
ApplicationUser currentUser = ComponentAccessor.jiraAuthenticationContext.getLoggedInUser()


writer.write("""
<li>
    <a class="sr-notes sr-trigger-dialog jira-my-notes-plugin" role="button" id="jira-my-notes-link"
        href="http://localhost:5500/sources/jira-dialog.html">
        <span class="aui-icon aui-icon-small aui-iconfont-lightbulb-filled my-notes-icon">
            My Notes
        </span>
        ${hasOverdue(currentUser) ? OVERDUE_STYLE : ""}
    </a>
</li>
""")

boolean hasOverdue(ApplicationUser user) {
    String jsonNotes = user.entityPropertiesOverrideSecurity.getJson(MYNOTES_PROPERTY_KEY)
    if (!jsonNotes) {
        return false
    }

    JSONArray jsonNotesArray = new JSONArray(jsonNotes)
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    LocalDateTime localDateTime = LocalDateTime.now()

    for (int i = 0; i < jsonNotesArray.length(); i++) {
        JSONObject note = jsonNotesArray.getJSONObject(i)
        if (note.has("dueDate") && !note.isNull("dueDate")) {
            String dueDateStr = note.getString("dueDate")
            if (!dueDateStr.isEmpty()) {
                try {
                    LocalDateTime dueDate = LocalDateTime.parse(dueDateStr, dateTimeFormatter)
                    if (dueDate.isBefore(localDateTime)) {
                        return true
                    }
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }
    return false
}