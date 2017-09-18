package in.tsdo.elw;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.os.AsyncTaskCompat;

import in.tsdo.elw.AsyncTasks.AppLoadIconLabelTask;

public class AppInfo {
    private ApplicationInfo info;
    private Drawable icon;
    private String appName;
    private AppLoadIconLabelTask task;
    private boolean isSystem;
    private boolean[] checked;
    public AppInfo(ApplicationInfo info, boolean[] checked, boolean isSystem) {
        this.info = info;
        this.checked = checked;
        this.isSystem = isSystem;
        this.appName = null;
        this.icon = null;
    }

    public void setChecked(int type, boolean checked) {
        this.checked[type] = checked;
    }

    public boolean isChecked(int type){
        return this.checked[type];
    }

    public boolean isSystem(){
        return isSystem;
    }

    public ApplicationInfo getInfo() {
        return this.info;
    }

    public String getAppName() {
        return appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setAppInfo(String appName) {
        this.appName = appName;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setTask(AppLoadIconLabelTask task){
        this.task = task;
    }

    public boolean hasTask() {
        return this.task != null;
    }
}
