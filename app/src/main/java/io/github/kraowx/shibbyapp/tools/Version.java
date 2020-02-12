package io.github.kraowx.shibbyapp.tools;

import org.json.JSONException;
import org.json.JSONObject;

public class Version
{
    private int semVer;
    private boolean preRelease;
    private String name;
    private String updateMessage;

    public Version(String name, boolean preRelease)
    {
        parseVersion(name);
        this.name = name;
        this.preRelease = preRelease;
    }

    public Version(JSONObject json)
    {
        try
        {
            parseVersion(json.getString("name"));
            name = json.getString("name");
            preRelease = json.getBoolean("prerelease");
            updateMessage = simpleMarkdownToHtml(json.getString("body"));
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isPreRelease()
    {
        return this.preRelease;
    }
    
    public String getUpdateMessage()
    {
        return updateMessage;
    }

    private void parseVersion(String rawVersion)
    {
        if (rawVersion != null)
        {
            rawVersion = rawVersion.replace("v", "");
            String[] semVerArr = rawVersion.split("\\.");
            String semVerStr = "";
            for (int i = 0; i < semVerArr.length; i++)
            {
                semVerStr += semVerArr[i];
            }
            semVer = Integer.parseInt(semVerStr);
        }
        else
        {
            semVer = -1;
        }
    }

    public boolean greater(Version other)
    {
        return this.semVer > other.semVer;
    }
    
    private static String simpleMarkdownToHtml(String markdownText)
    {
        char c;
        int j, k;
        int listLevel = 0, level = 0, lastLevel = 0;
        markdownText = markdownText.replace("\\r", "\r");
        markdownText = markdownText.replace("\\n", "\n");
        char[] chars = markdownText.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            c = chars[i];
            if (c == '#')
            {
                j = i;
                k = 0;
                while (chars[j] == '#')
                {
                    // 11 = replace with empty
                    chars[j++] = 0x11;
                    k++;
                }
                // 1-5 = replace with <h[1-5]>
                chars[j] = (char)(k);
                while (chars[++j] != '\r')
                    ;
                // 12 = replace with </h1-5>
                chars[j] = (char)(k+5);
                j++;
                chars[j] = 0x12;
                i+=j;
            }
            else if (c == '`')
            {
                // 17 = replace with <code>
                chars[i] = 0x17;
                i++;
                k = 1;
                while (chars[i] == '`')
                {
                    // 11 = replace with empty
                    chars[i++] = 0x11;
                    k++;
                }
                if (k == 3)
                {
                    while (chars[++i] != '`')
                        ;
                    // 18 = replace with </code>
                    chars[i] = 0x18;
                    i++;
                    while (chars[i] == '`')
                    {
                        // 11 = replace with empty
                        chars[i++] = 0x11;
                    }
                }
            }
            else if (c == '*')
            {
                if (chars[i+1] == '*')
                {
                    // 15 = replace with <i>
                    chars[i] = 0x15;
                    // 11 = replace with empty
                    chars[i+1] = 0x11;
                }
                else
                {
                    // 13 = replace with <b>
                    chars[i] = 0x13;
                }
                while (chars[++i] != '*')
                    ;
                if (chars[i+1] == '*')
                {
                    // 16 = replace with </i>
                    chars[i] = 0x16;
                    // 11 = replace with empty
                    chars[i+1] = 0x11;
                }
                else
                {
                    // 14 = replace with </b>
                    chars[i] = 0x14;
                }
            }
            else if (c == '-')
            {
                j = i;
                // Set up the initial list
                if (listLevel == 0)
                {
                    // 19 = replace with <ul>
                    chars[j-1] = 0x19;
                    listLevel++;
                }
                // 1B = replace with <li>
                chars[j] = 0x1B;
                
                /*
                 * Concatenate a string of indents to find
                 * the indent level of the list item
                 */
                String indent = "";
                int last = -1;
                int l = j-5;
                int m = j-1;
                while (getIndentLevel(indent) != last)
                {
                    last = getIndentLevel(indent);
                    indent += new String(chars).replace('\r', ' ')
                            .replace('\n', ' ').substring(l, m);
                    l -= 4;
                    m -= 4;
                }
                level = last;
                
                // Current level is indented more than last
                if (level > lastLevel)
                {
                    // 19 = replace with <ul>
                    chars[j-1] = 0x19;
                    listLevel++;
                }
                // Current level is indented less than last
                else if (level < lastLevel)
                {
                    // 1A = replace with </ul>
                    chars[j-1] = 0x1A;
                    listLevel--;
                }
                
                // Search until the end of the list item is found
                while (j < chars.length && chars[j] != '\r')
                    j++;
                
                if (j < chars.length)
                {
                    // 1C = replace with </li>
                    chars[j] = 0x1C;
                }
                
                lastLevel = level;
            }
        }
        markdownText = new String(chars);
        if (listLevel > 0)
        {
            markdownText += "\u001C";
        }
        while (listLevel-- > 0)
        {
            markdownText += "\u001A";
        }
        for (int i = 0; i < 4; i++)
        {
            String code = (char)(0x0 + i+1) + "";
            markdownText = markdownText.replaceAll(code, "<h" + (i+1) + ">");
        }
        for (int i = 5; i < 9; i++)
        {
            String code = (char)(0x0 + i+1) + "";
            markdownText = markdownText.replaceAll(code, "</h" + (i+1-5) + ">");
        }
        markdownText = markdownText.replaceAll("\u0011", "");
        markdownText = markdownText.replaceAll("\u0012", "<br>");
        markdownText = markdownText.replaceAll("\u0013", "<b>");
        markdownText = markdownText.replaceAll("\u0014", "</b>");
        markdownText = markdownText.replaceAll("\u0015", "<i>");
        markdownText = markdownText.replaceAll("\u0016", "</i>");
        markdownText = markdownText.replaceAll("\u0017", "<code>");
        markdownText = markdownText.replaceAll("\u0018", "</code>");
        markdownText = markdownText.replaceAll("\u0019", "<ul>");
        markdownText = markdownText.replaceAll("\u001A", "</ul>");
        markdownText = markdownText.replaceAll("\u001B", "<li>");
        markdownText = markdownText.replaceAll("\u001C", "</li>");
        markdownText = markdownText.replace("\r\n", "<br>");
        markdownText = markdownText.replace("\\\"", "\"");
        return markdownText;
    }
    
    private static int getIndentLevel(String indent)
    {
        int j = 0;
        char[] chars = indent.toCharArray();
        for (int i = 0; i < chars.length && chars[i] == ' '; i++, j++)
            ;
        if (j % 4 == 0)
        {
            return j/4;
        }
        return 0;
    }
}
