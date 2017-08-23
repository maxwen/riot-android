package im.vector.util;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TypefaceSpan;

import org.xml.sax.XMLReader;

import java.util.Stack;

import im.vector.R;

public class MyConsoleHtmlTagHandler implements Html.TagHandler {
    Stack<String> lists = new Stack();
    Stack<Integer> olNextIndex = new Stack();
    StringBuilder tableHtmlBuilder = new StringBuilder();
    int tableTagLevel = 0;
    private static final int indent = 10;
    private static final int listItemIndent = 20;
    private static final BulletSpan bullet = new BulletSpan(10);
    public Context mContext;

    public MyConsoleHtmlTagHandler() {
    }

    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (opening) {
            if (tag.equalsIgnoreCase("ul")) {
                this.lists.push(tag);
            } else if (tag.equalsIgnoreCase("ol")) {
                this.lists.push(tag);
                this.olNextIndex.push(Integer.valueOf(1));
            } else if (tag.equalsIgnoreCase("li")) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != 10) {
                    output.append("\n");
                }

                String numberMargin = (String) this.lists.peek();
                if (numberMargin.equalsIgnoreCase("ol")) {
                    this.start(output, new MyConsoleHtmlTagHandler.Ol());
                    output.append(((Integer) this.olNextIndex.peek()).toString()).append(". ");
                    this.olNextIndex.push(Integer.valueOf(((Integer) this.olNextIndex.pop()).intValue() + 1));
                } else if (numberMargin.equalsIgnoreCase("ul")) {
                    this.start(output, new MyConsoleHtmlTagHandler.Ul());
                }
            } else if (tag.equalsIgnoreCase("code")) {
                this.start(output, new MyConsoleHtmlTagHandler.Code());
            } else if (tag.equalsIgnoreCase("center")) {
                this.start(output, new MyConsoleHtmlTagHandler.Center());
            } else if (!tag.equalsIgnoreCase("s") && !tag.equalsIgnoreCase("strike")) {
                if (tag.equalsIgnoreCase("table")) {
                    this.start(output, new MyConsoleHtmlTagHandler.Table());
                    if (this.tableTagLevel == 0) {
                        this.tableHtmlBuilder = new StringBuilder();
                        output.append("table placeholder");
                    }

                    ++this.tableTagLevel;
                } else if (tag.equalsIgnoreCase("tr")) {
                    this.start(output, new MyConsoleHtmlTagHandler.Tr());
                } else if (tag.equalsIgnoreCase("th")) {
                    this.start(output, new MyConsoleHtmlTagHandler.Th());
                } else if (tag.equalsIgnoreCase("td")) {
                    this.start(output, new MyConsoleHtmlTagHandler.Td());
                }
            } else {
                this.start(output, new MyConsoleHtmlTagHandler.Strike());
            }
        } else if (tag.equalsIgnoreCase("ul")) {
            this.lists.pop();
        } else if (tag.equalsIgnoreCase("ol")) {
            this.lists.pop();
            this.olNextIndex.pop();
        } else if (tag.equalsIgnoreCase("li")) {
            int var7;
            if (((String) this.lists.peek()).equalsIgnoreCase("ul")) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != 10) {
                    output.append("\n");
                }

                var7 = 10;
                if (this.lists.size() > 1) {
                    var7 = 10 - bullet.getLeadingMargin(true);
                    if (this.lists.size() > 2) {
                        var7 -= (this.lists.size() - 2) * 20;
                    }
                }

                BulletSpan newBullet = new BulletSpan(var7);
                this.end(output, MyConsoleHtmlTagHandler.Ul.class, false, new Object[]{new LeadingMarginSpan.Standard(20 * (this.lists.size() - 1)), newBullet});
            } else if (((String) this.lists.peek()).equalsIgnoreCase("ol")) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != 10) {
                    output.append("\n");
                }

                var7 = 20 * (this.lists.size() - 1);
                if (this.lists.size() > 2) {
                    var7 -= (this.lists.size() - 2) * 20;
                }

                this.end(output, MyConsoleHtmlTagHandler.Ol.class, false, new Object[]{new LeadingMarginSpan.Standard(var7)});
            }
        } else if (tag.equalsIgnoreCase("code")) {
            this.end(output, MyConsoleHtmlTagHandler.Code.class, false, new Object[]{new BackgroundColorSpan(this.mContext.getResources().getColor(R.color.markdown_code_background)), new TypefaceSpan("monospace")});
        } else if (tag.equalsIgnoreCase("center")) {
            this.end(output, MyConsoleHtmlTagHandler.Center.class, true, new Object[]{new android.text.style.AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)});
        } else if (!tag.equalsIgnoreCase("s") && !tag.equalsIgnoreCase("strike")) {
            if (tag.equalsIgnoreCase("table")) {
                --this.tableTagLevel;
                this.end(output, MyConsoleHtmlTagHandler.Table.class, false, new Object[0]);
            } else if (tag.equalsIgnoreCase("tr")) {
                this.end(output, MyConsoleHtmlTagHandler.Tr.class, false, new Object[0]);
            } else if (tag.equalsIgnoreCase("th")) {
                this.end(output, MyConsoleHtmlTagHandler.Th.class, false, new Object[0]);
            } else if (tag.equalsIgnoreCase("td")) {
                this.end(output, MyConsoleHtmlTagHandler.Td.class, false, new Object[0]);
            }
        } else {
            this.end(output, MyConsoleHtmlTagHandler.Strike.class, false, new Object[]{new StrikethroughSpan()});
        }

        this.storeTableTags(opening, tag);
    }

    private void storeTableTags(boolean opening, String tag) {
        if (this.tableTagLevel > 0 || tag.equalsIgnoreCase("table")) {
            this.tableHtmlBuilder.append("<");
            if (!opening) {
                this.tableHtmlBuilder.append("/");
            }

            this.tableHtmlBuilder.append(tag.toLowerCase()).append(">");
        }

    }

    private void start(Editable output, Object mark) {
        int len = output.length();
        output.setSpan(mark, len, len, 17);
    }

    private void end(Editable output, Class kind, boolean paragraphStyle, Object... replaces) {
        Object obj = getLast(output, kind);
        int where = output.getSpanStart(obj);
        int len = output.length();
        if (this.tableTagLevel > 0) {
            CharSequence thisLen = this.extractSpanText(output, kind);
            this.tableHtmlBuilder.append(thisLen);
        }

        output.removeSpan(obj);
        if (where != len) {
            int var13 = len;
            if (paragraphStyle) {
                output.append("\n");
                var13 = len + 1;
            }

            Object[] var9 = replaces;
            int var10 = replaces.length;

            for (int var11 = 0; var11 < var10; ++var11) {
                Object replace = var9[var11];
                output.setSpan(replace, where, var13, 33);
            }
        }

    }

    private CharSequence extractSpanText(Editable output, Class kind) {
        Object obj = getLast(output, kind);
        int where = output.getSpanStart(obj);
        int len = output.length();
        CharSequence extractedSpanText = output.subSequence(where, len);
        output.delete(where, len);
        return extractedSpanText;
    }

    private static Object getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        } else {
            for (int i = objs.length; i > 0; --i) {
                if (text.getSpanFlags(objs[i - 1]) == 17) {
                    return objs[i - 1];
                }
            }

            return null;
        }
    }

    private static class Td {
        private Td() {
        }
    }

    private static class Th {
        private Th() {
        }
    }

    private static class Tr {
        private Tr() {
        }
    }

    private static class Table {
        private Table() {
        }
    }

    private static class Strike {
        private Strike() {
        }
    }

    private static class Center {
        private Center() {
        }
    }

    private static class Code {
        private Code() {
        }
    }

    private static class Ol {
        private Ol() {
        }
    }

    private static class Ul {
        private Ul() {
        }
    }
}
