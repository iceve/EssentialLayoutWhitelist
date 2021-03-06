package in.tsdo.elw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.HashMap;

import in.tsdo.elw.AsyncTasks.AppPackageLoader;

public class AppPickerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<HashMap<String, AppInfo>>{

    private Toolbar toolbar;
    private AppPackagePacker appPacker;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private boolean showSystem;
    private SearchView searchView;
    MenuItem showSystemMenu;
    Parcelable state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = getSharedPreferences("ESSENTIAL_TOOLS_PREF", MODE_PRIVATE);
        showSystem = pref.getBoolean("SHOW_SYSTEM", false);
        appPacker = new AppPackagePacker(getFillString(), getHideString(), showSystem);

        getSupportLoaderManager().initLoader(0, null, this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        createFragmentPageAdapter(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        notifyFragmentNewPacker(appPacker);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (state != null) {
            // If resumed from pause.
            SharedPreferences pref = getSharedPreferences("ESSENTIAL_TOOLS_PREF", MODE_PRIVATE);
            showSystem = pref.getBoolean("SHOW_SYSTEM", false);
            viewPager.onRestoreInstanceState(state);

        }
    }

    @Override
    protected void onPause() {
        writeSettingString();
        state = viewPager.onSaveInstanceState();
        SharedPreferences.Editor edit = getSharedPreferences("ESSENTIAL_TOOLS_PREF", MODE_PRIVATE).edit();
        edit.putBoolean("SHOW_SYSTEM", showSystem);
        edit.apply();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_app_picker, menu);
        showSystemMenu = menu.findItem(R.id.menu_show_system);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return false;}

            @Override
            public boolean onQueryTextChange(String newText) {
                if (appPacker != null && appPacker.isApplied()) {
                    appPacker.filter(newText);
                    notifyFragmentDataSetChanged();
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int currentPage = viewPager.getCurrentItem();
        // TODO: Hmm, ever want your whitelist back?
        if (appPacker == null) return true;
        switch (item.getItemId()) {
            case R.id.menu_restore:
                //appPacker.setFillString(MainActivity.DEFAULT_WHITELIST);
                appPacker.setCheckedAll(AppPackagePacker.HIDE, false);
                notifyFragmentDataSetChanged(currentPage);
                return true;
            case R.id.menu_deselect_all:

                appPacker.setCheckedAll(AppPackagePacker.HIDE, false);
                notifyFragmentDataSetChanged(currentPage);
                return true;
            case R.id.menu_select_all:
                appPacker.setCheckedAll(AppPackagePacker.HIDE, true);
                notifyFragmentDataSetChanged(currentPage);
                return true;
            case R.id.menu_invert:
                appPacker.invertSelection(AppPackagePacker.HIDE);
                notifyFragmentDataSetChanged(currentPage);
                return true;
            case R.id.menu_show_system:
                showSystem = !showSystem;
                appPacker.setShowSystem(showSystem);
                notifyFragmentDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (showSystem) {
            showSystemMenu.setTitle(R.string.menu_hide_system);
        }
        else {
            showSystemMenu.setTitle(R.string.menu_show_system);
        }
        if (appPacker == null) {
            menu.setGroupEnabled(0, false);
        }
        else {
            menu.setGroupEnabled(0, true);
        }
        return true;
    }

    protected String getFillString() {
        // TODO: hey whitelist
        return "";
        /*
        String settingString = Settings.Global.getString(getContentResolver(), "ESSENTIAL_LAYOUT_WHITELIST");
        if (settingString == null) {
            settingString = MainActivity.DEFAULT_WHITELIST;
        }
        if (!settingString.endsWith(",")) {
            settingString += ",";
        }
        return settingString;
        */
    }

    protected String getHideString() {
        String settingString = Settings.Global.getString(getContentResolver(), "policy_control");
        if (settingString == null) {
            settingString = "immersive.navigation=";
        }
        if (!settingString.endsWith(",")) {
            settingString += ",";
        }
        return settingString;
    }

    protected AppPackagePacker getAppPacker() {
        return appPacker;
    }

    protected void writeSettingString() {
        if (appPacker == null || !appPacker.isApplied()) return;
        //Settings.Global.putString(getContentResolver(), "ESSENTIAL_LAYOUT_WHITELIST", appPacker.generateFillString());
        Settings.Global.putString(getContentResolver(), "policy_control", appPacker.generateHideString());
    }

    private void createFragmentPageAdapter(ViewPager viewPager) {
        ListViewFragPageAdapter adapter = new ListViewFragPageAdapter(getSupportFragmentManager());
        // Add fragment here
        adapter.addFragment(newFragment(AppPackagePacker.HIDE), getString(R.string.tab_hide_nav));
        viewPager.setAdapter(adapter);
    }

    private ListViewFragment newFragment(int type) {
        Bundle args = new Bundle();
        args.putInt("FRAGMENT_TYPE", type);

        ListViewFragment fragment = new ListViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void notifyFragmentDataSetChanged() {
        if (viewPager == null || viewPager.getAdapter() == null)
            return;
        viewPager.getAdapter().notifyDataSetChanged();
    }

    private void notifyFragmentDataSetChanged(int page) {
        if (viewPager == null || viewPager.getAdapter() == null)
            return;
        ((ListViewFragPageAdapter)viewPager.getAdapter()).notifyFragmentDataSetChanged(page);
    }

    private void notifyFragmentNewPacker(AppPackagePacker appPacker) {
        if (viewPager == null || viewPager.getAdapter() == null)
            return;
        ((ListViewFragPageAdapter)viewPager.getAdapter()).notifyNewPacker(appPacker);
        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public Loader<HashMap<String, AppInfo>> onCreateLoader(int id, Bundle args) {
        return new AppPackageLoader(getApplicationContext(), appPacker);
    }

    @Override
    public void onLoadFinished(Loader<HashMap<String, AppInfo>> loader, HashMap<String, AppInfo> data) {
        if (data == null)
            return;
        appPacker.applyAppMap(data);
        if (searchView.getQuery() != null) {
            if (appPacker != null && appPacker.isApplied()) {
                appPacker.filter(searchView.getQuery().toString());
            }
        }
        notifyFragmentDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<HashMap<String, AppInfo>> loader) {

    }
}
