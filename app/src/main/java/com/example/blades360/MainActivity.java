/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.app.Activity
 *  android.content.Context
 *  android.content.Intent
 *  android.content.res.AssetManager
 *  android.graphics.Bitmap
 *  android.graphics.Canvas
 *  android.graphics.LinearGradient
 *  android.graphics.Paint
 *  android.graphics.Paint$Align
 *  android.graphics.Paint$FontMetrics
 *  android.graphics.Paint$Style
 *  android.graphics.Path
 *  android.graphics.Path$Direction
 *  android.graphics.RadialGradient
 *  android.graphics.RectF
 *  android.graphics.Shader
 *  android.graphics.Shader$TileMode
 *  android.graphics.Typeface
 *  android.media.SoundPool
 *  android.media.SoundPool$Builder
 *  android.os.Build$VERSION
 *  android.os.Bundle
 *  android.view.InputDevice
 *  android.view.InputDevice$MotionRange
 *  android.view.KeyEvent
 *  android.view.MotionEvent
 *  android.view.View
 *  android.view.WindowManager$LayoutParams
 *  com.example.blades360.R$raw
 */
package com.example.blades360;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.example.blades360.EmulatorLibrary;
import com.example.blades360.R;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity
extends Activity {
    Dashboard dashboard;
    EmulatorLibrary library;
    AppMedia media;
    FrontendTools tools;
    ProfileSetup profile;
    BootAnimationView bootAnimation;
    RetroAchievements retroAchievements;
    RetroMarketplace marketplace;
    RommClient romm; RommMarketplace rommMarketplace;
    JellyfinClient jellyfin; PlexClient plex; JellyfinBrowser jellyfinBrowser;
    BladesVpn vpn;
    YoutubeClient youtube; YoutubeBrowser youtubeBrowser;
    void openYoutube(){if(youtubeBrowser!=null)return;tools.close();media.active=false;dashboard.stopStick();youtubeBrowser=new YoutubeBrowser(this);addContentView(youtubeBrowser,new android.view.ViewGroup.LayoutParams(-1,-1));youtubeBrowser.requestFocus();}
    IptvClient iptv; IptvBrowser iptvBrowser;
    void openIptv(){if(iptvBrowser!=null)return;tools.close();media.active=false;dashboard.stopStick();iptvBrowser=new IptvBrowser(this);addContentView(iptvBrowser,new android.view.ViewGroup.LayoutParams(-1,-1));iptvBrowser.requestFocus();}
    void openJellyfin(){if(jellyfinBrowser!=null)return;tools.close();media.active=false;media.tools.open=false;dashboard.optionMode=0;dashboard.stopStick();jellyfinBrowser=new JellyfinBrowser(this);addContentView(jellyfinBrowser,new android.view.ViewGroup.LayoutParams(-1,-1));jellyfinBrowser.requestFocus();}
    void openMarketplace(){if(rommMarketplace!=null)return;tools.close();dashboard.optionMode=0;dashboard.stopStick();rommMarketplace=new RommMarketplace(this);addContentView(rommMarketplace,new android.view.ViewGroup.LayoutParams(-1,-1));rommMarketplace.requestFocus();if(romm.games.length()==0&&!romm.server().isEmpty())romm.refresh();}
    void openAchievements(){if(marketplace!=null)return;tools.close();dashboard.optionMode=0;dashboard.stopStick();marketplace=new RetroMarketplace(this);addContentView(marketplace,new android.view.ViewGroup.LayoutParams(-1,-1));marketplace.requestFocus();if(retroAchievements.connected()&&System.currentTimeMillis()-retroAchievements.updated>300000)retroAchievements.refresh();}

    public void onCreate(Bundle b) {
        super.onCreate(b);
        this.setRequestedOrientation(6);
        this.getWindow().getDecorView().setSystemUiVisibility(5894);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams p = this.getWindow().getAttributes();
            p.layoutInDisplayCutoutMode = 1;
            this.getWindow().setAttributes(p);
        }
        this.library = new EmulatorLibrary(this);
        this.dashboard = new Dashboard();
        this.media = new AppMedia(this);
        this.tools = new FrontendTools(this);
        this.profile = new ProfileSetup(this);
        this.retroAchievements = new RetroAchievements(this);
        this.romm = new RommClient(this);
        this.jellyfin = new JellyfinClient(this);
        this.plex = new PlexClient(this);this.iptv=new IptvClient(this);this.youtube=new YoutubeClient(this);this.vpn=BladesVpn.get(this);vpn.changed=()->vpn.changed(this);
        this.dashboard.blade=Math.max(0,Math.min(3,profile.prefs.getInt("boot_blade",1)));
        library.launchIssue=issue->tools.launchIssue(issue);
        this.setContentView(this.dashboard);
        this.library.openView = () -> this.dashboard.openLibrary();
        this.library.artChanged = () -> this.dashboard.invalidate();
        library.launchConfirmation=g->{dashboard.optionGame=g;dashboard.openSystem(17);};library.detailsView=g->dashboard.openOptions(g);library.emulatorView=(g,play)->dashboard.openEmulators(g,play,false);
        if(b==null){bootAnimation=new BootAnimationView(this,library.prefs.getBoolean("sound_enabled",true),library.prefs.getInt("sound_volume",100)/100f,()->{bootAnimation=null;dashboard.requestFocus();if(!isFinishing()&&!profile.prefs.getBoolean("complete",false))profile.start();});addContentView(bootAnimation,new android.view.ViewGroup.LayoutParams(-1,-1));bootAnimation.requestFocus();}
        else if(!profile.prefs.getBoolean("complete",false))dashboard.post(()->profile.start());
    }

    public void onBackPressed() {
        if(bootAnimation!=null){bootAnimation.finish();return;}
        if(ConsoleLaunchView.cancel(this))return;
        if(tools!=null&&tools.editor!=null){tools.closeEditor();return;}
        if(youtubeBrowser!=null){youtubeBrowser.back();return;}
        if(iptvBrowser!=null){iptvBrowser.back();return;}
        if(jellyfinBrowser!=null){jellyfinBrowser.back();return;}
        if(rommMarketplace!=null){rommMarketplace.back();return;}
        if(marketplace!=null){marketplace.back();return;}
        if(media!=null&&media.tools.editor!=null){media.tools.closeEditor();return;}
        this.dashboard.back();
    }

    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if(tools!=null&&tools.fullBackup!=null&&tools.fullBackup.result(request,result,data))return;
        if(vpn!=null&&vpn.result(request,result))return;
        if(iptv!=null&&iptv.result(request,result,data))return;
        if(profile!=null&&profile.result(request,result,data))return;
        if(EmulationSetup.result(this,request,result,data))return;
        if(tools!=null&&tools.backup.result(request,result,data))return;
        if (media != null && media.result(request,result,data)) return;
        if (this.library != null) {
            this.library.result(request, result, data);
        }
    }

    protected void onResume() {
        super.onResume();
        if(retroAchievements!=null&&retroAchievements.connected()&&System.currentTimeMillis()-retroAchievements.updated>300000)retroAchievements.refresh();
        if(bootAnimation!=null){bootAnimation.resume();bootAnimation.requestFocus();return;}
        if (this.dashboard != null) {
            if(tools!=null&&tools.editor!=null)tools.editor.requestFocus();
            else if(youtubeBrowser!=null){if(youtubeBrowser.player!=null)youtubeBrowser.player.requestFocus();else youtubeBrowser.requestFocus();}
            else if(iptvBrowser!=null){if(iptvBrowser.player!=null)iptvBrowser.player.requestFocus();else if(iptvBrowser.guide!=null)iptvBrowser.guide.requestFocus();else iptvBrowser.requestFocus();}
            else if(jellyfinBrowser!=null){if(jellyfinBrowser.player!=null)jellyfinBrowser.player.requestFocus();else jellyfinBrowser.requestFocus();}
            else if(rommMarketplace!=null)rommMarketplace.requestFocus();
            else if(marketplace!=null)marketplace.requestFocus();
            else if(media!=null&&media.tools.editor!=null)media.tools.editor.requestFocus();
            else this.dashboard.requestFocus();
            this.dashboard.invalidate();
        }
    }

    protected void onPause() {
        if(bootAnimation!=null)bootAnimation.pause();
        if (media != null) media.pause();
        if (this.dashboard != null) {
            this.dashboard.stopStick();
        }
        super.onPause();
        if(youtubeBrowser!=null&&youtubeBrowser.player!=null)youtubeBrowser.player.pause();
        if(jellyfinBrowser!=null&&jellyfinBrowser.player!=null)jellyfinBrowser.player.pause();
        if(iptvBrowser!=null&&iptvBrowser.player!=null)iptvBrowser.player.pause();
        if(iptvBrowser!=null&&iptvBrowser.guide!=null)iptvBrowser.guide.stopPreview();
    }

    protected void onDestroy() { if(vpn!=null)vpn.changed=()->{}; if(youtubeBrowser!=null){youtubeBrowser.closed=true;youtubeBrowser.ra.changed=()->{};if(youtubeBrowser.player!=null)youtubeBrowser.player.close();}if(youtube!=null)youtube.close(); if(retroAchievements!=null)retroAchievements.close();if(romm!=null)romm.close();if(jellyfinBrowser!=null)jellyfinBrowser.close();if(jellyfin!=null)jellyfin.close();if(plex!=null)plex.close();if(iptvBrowser!=null)iptvBrowser.close(false);if(iptv!=null)iptv.close();if(bootAnimation!=null)bootAnimation.dispose();if(tools!=null){tools.art.close();tools.backup.close();} if(media!=null)media.close(); super.onDestroy(); }

    class Dashboard
    extends View {
        final String[] names;
        final String[][] rows;
        final int[] topColors;
        final int[] midColors;
        final int[] bottomColors;
        final int[] headerColors;
        final Paint p;
        final Typeface regular;
        final Typeface bold;
        SoundPool sounds;
        int navSound;
        int bladeSound;
        int selectSound;
        int backSound;
        final float[] bladeLeft;
        final float[] bladeRight;
        final float[] cardX;
        final float[] cardY;
        final float[] menuLeft;
        final float[] menuEnd;
        final float[] menuTop;
        final float[] descriptionLeft;
        final float[] titleX;
        final float[] titleY;
        int blade;
        int row;
        float scale;
        float W;
        float H;
        float offsetX;
        float left;
        float right;
        float menuX;
        float menuRight;
        float rowY;
        float rowStep;
        float downX;
        float downY;
        boolean dialog;
        String title;
        String message;
        boolean libraryMode;
        int gameIndex;
        int gameOffset;
        float gameTop;
        float gameStep;
        int gameSlots;
        int stickX;
        int stickY;
        final Runnable stickRepeat;

        void openLibrary() {
            this.optionMode = 0;
            this.blade = 1;
            this.libraryMode = true;
            this.dialog = false;
            this.stopStick();
            this.requestFocus();
            this.invalidate();
        }

        ArrayList<EmulatorLibrary.Game> visibleGames() {
            return MainActivity.this.library.visibleGames();
        }

        void clampGames(int count) {
            this.gameIndex = Math.max(0, Math.min(this.gameIndex, count - 1));
            this.gameOffset = Math.max(0, Math.min(this.gameOffset, Math.max(0, count - this.gameSlots)));
            if (this.gameIndex < this.gameOffset) {
                this.gameOffset = this.gameIndex;
            }
            if (this.gameIndex >= this.gameOffset + this.gameSlots) {
                this.gameOffset = this.gameIndex - this.gameSlots + 1;
            }
        }

        void filterGames(int delta) {
            ArrayList<String> filters = MainActivity.this.library.filters();
            int index = filters.indexOf(MainActivity.this.library.filter);
            MainActivity.this.library.filter = filters.get((Math.max(0, index) + delta + filters.size()) % filters.size());
            this.gameOffset = 0;
            this.gameIndex = 0;
            this.sound(this.navSound);
            this.invalidate();
        }

        void libraryActions() {
            ArrayList<EmulatorLibrary.Game> list = this.visibleGames();
            if (list.isEmpty()) {
                tools.openHome();
                return;
            }
            this.clampGames(list.size());
            MainActivity.this.library.details(list.get(this.gameIndex));
        }

        void libraryPlay() {
            ArrayList<EmulatorLibrary.Game> list = this.visibleGames();
            if (list.isEmpty()) {
                tools.openHome();
                return;
            }
            this.clampGames(list.size());
            EmulatorLibrary.Game game = list.get(this.gameIndex);
            this.sound(this.selectSound);
            if (library.effectiveEmulator(game).isEmpty()) {
                MainActivity.this.library.chooseEmulator(game, true);
            } else {
                MainActivity.this.library.launch(game);
            }
        }

        String fit(String value, float width, float size) {
            this.p.setTypeface(this.regular);
            this.p.setTextSize(size);
            int count = this.p.breakText(value, true, width, null);
            return count < value.length() ? value.substring(0, Math.max(0, count - 2)) + "\u2026" : value;
        }

        void drawLibrary(Canvas c) {
            this.text(c, "Games / " + MainActivity.this.library.filter, this.titleX[1], this.titleY[1], 35.0f, -722449, false, true);
            this.menuX = 220.0f;
            this.menuRight = 720.0f;
            this.gameTop = 154.0f;
            this.gameStep = 54.0f;
            this.gameSlots = Math.max(3, Math.min(12, (int)((this.H - 260.0f - this.gameTop) / this.gameStep) + 1));
            ArrayList<EmulatorLibrary.Game> list = this.visibleGames();
            this.clampGames(list.size());
            this.text(c, "\u25c0  " + MainActivity.this.library.filter + "  \u25b6", this.menuX, 115.0f, 25.0f, -1576228, false, true);
            this.text(c, list.size() + " game" + (list.size() == 1 ? "" : "s"), 835.0f, 115.0f, 23.0f, -2298922, false, true);
            text(c,"START / Tools",menuX,H-115,20,0xffecf5de,false,true);
            text(c,fit(library.searchQuery.isEmpty()?"Sort: "+library.sortOrder:"Search: "+library.searchQuery,430,18),menuX+210,H-115,18,0xffecf5de,false,true);
            if (list.isEmpty()) {
                this.text(c, "No games in this view", this.menuX, this.gameTop + 35.0f, 30.0f, -15320549, false, false);
                this.wrapped(c, MainActivity.this.library.games.isEmpty() ? "Press Y to add game files or scan your ROMS folder." : "Use left or right to switch the library view.", this.menuX, this.gameTop + 90.0f, 470.0f, 25.0f, -14397654);
            } else {
                for (int i = this.gameOffset; i < Math.min(list.size(), this.gameOffset + this.gameSlots); ++i) {
                    EmulatorLibrary.Game game = list.get(i);
                    float y = this.gameTop + (float)(i - this.gameOffset) * this.gameStep;
                    if (i == this.gameIndex) {
                        this.gradient(c, this.menuX - 7.0f, y - 3.0f, this.menuRight, y + 48.0f, -857015847, -2003713158, true, 9.0f);
                    }
                    this.line(c, this.menuX, y + 49.0f, this.menuRight, y + 49.0f, 1716745295, 2.0f);
                    this.text(c, game.favorite ? "\u2605" : "\u25cf", this.menuX + 10.0f, y + 33.0f, 21.0f, -1050394, false, true);
                    this.text(c, this.fit(library.displayTitle(game), this.menuRight - this.menuX - 55.0f, 27.0f), this.menuX + 44.0f, y + 34.0f, 27.0f, -15520489, false, false);
                }
                EmulatorLibrary.Game selected = list.get(this.gameIndex);
                float dx = 760.0f;
                float artHeight = Math.min(270.0f, this.H - this.gameTop - 305.0f);
                Bitmap cover = MainActivity.this.library.thumbnail(selected);
                this.gradient(c, dx, this.gameTop, 1090.0f, this.gameTop + artHeight, 1147373386, 1720498038, true, 10.0f);
                if (cover != null) {
                    float ratio = Math.min(330.0f / (float)cover.getWidth(), artHeight / (float)cover.getHeight());
                    float bw = (float)cover.getWidth() * ratio;
                    float bh = (float)cover.getHeight() * ratio;
                    this.fill(-1);
                    this.p.setFilterBitmap(true);
                    c.drawBitmap(cover, null, new RectF(dx + (330.0f - bw) / 2.0f, this.gameTop + (artHeight - bh) / 2.0f, dx + (330.0f + bw) / 2.0f, this.gameTop + (artHeight + bh) / 2.0f), this.p);
                    String badge = PackageArtwork.badge(selected.system, selected.name);
                    if (!badge.isEmpty()) {
                        float left = dx + (330.0f - bw) / 2.0f;
                        float bottom = this.gameTop + (artHeight + bh) / 2.0f;
                        this.gradient(c, left, bottom - 38.0f, left + bw, bottom,
                                badge.equals("DLC") ? 0xf02d527d : 0xf065741c,
                                badge.equals("DLC") ? 0xf0132848 : 0xf02e3b0d, true, 0.0f);
                        this.text(c, badge, left + 12.0f, bottom - 10.0f, 23.0f, -1, true, true);
                    }
                } else {
                    this.text(c, this.fit(selected.system, 300.0f, 27.0f), dx + 15.0f, this.gameTop + artHeight / 2.0f, 27.0f, -2626869, true, true);
                    this.text(c, MainActivity.this.library.artworkStatus(selected), dx + 35.0f, this.gameTop + artHeight / 2.0f + 35.0f, 21.0f, -2626869, false, false);
                }
                c.save();
                c.clipRect(dx, this.gameTop + artHeight + 10.0f, 1095.0f, this.H - 155.0f);
                this.wrapped(c, this.fit(EmulatorLibrary.cleanTitle(selected.name), 330.0f, 27.0f), dx, this.gameTop + artHeight + 42.0f, 330.0f, 27.0f, -15387365);
                this.text(c, selected.system, dx, this.gameTop + artHeight + 80.0f, 22.0f, -13542859, false, false);
                String emulator = library.emulatorLabel(selected);
                if (!library.effectiveEmulator(selected).isEmpty()) {
                    try {
                        emulator = library.emulatorLabel(selected);
                    }
                    catch (Exception ignored) {
                        emulator = "Emulator unavailable";
                    }
                }
                this.wrapped(c, emulator, dx, this.gameTop + artHeight + 118.0f, 330.0f, 22.0f, -13542859);
                c.restore();
                this.text(c, this.gameIndex + 1 + " / " + list.size(), this.menuX, this.H - 135.0f, 23.0f, -13412808, false, false);
                if (this.gameOffset > 0) {
                    this.text(c, "\u25b2", this.menuRight - 27.0f, this.gameTop - 14.0f, 19.0f, -1444129, false, false);
                }
                if (this.gameOffset + this.gameSlots < list.size()) {
                    this.text(c, "\u25bc", this.menuRight - 27.0f, this.gameTop + (float)this.gameSlots * this.gameStep + 14.0f, 19.0f, -1444129, false, false);
                }
            }
            this.gradient(c, this.left + 20.0f, this.H - 96.0f, this.right, this.H + 20.0f, 0x66FFFFFF, 1148223322, true, 24.0f);
            String[] keys = new String[]{"A", "B", "X", "Y"};
            String[] labels = new String[]{list.isEmpty() ? "Add games" : "Play", "Back", "Options", "Add games"};
            int[] colors = new int[]{-8145090, -4439495, -14647870, -3101390};
            for (int i = 0; i < 4; ++i) {
                float x = 240 + i * 220;
                this.button(c, x, this.H - 39.0f, keys[i], colors[i]);
                this.text(c, labels[i], x + 25.0f, this.H - 31.0f, 24.0f, -920343, false, true);
            }
        }

        boolean libraryTouch(float x, float y) {
            int choice;
            if (y > this.H - 96.0f) {
                int action = Math.max(0, Math.min(3, (int)((x - 200.0f) / 220.0f)));
                if (action == 0) {
                    this.libraryPlay();
                } else if (action == 1) {
                    this.back();
                } else if (action == 2) {
                    this.libraryActions();
                } else {
                    MainActivity.this.library.importGames();
                }
                return true;
            }
            if (y > 76.0f && y < 137.0f) {
                this.filterGames(x < 470.0f ? -1 : 1);
                return true;
            }
            ArrayList<EmulatorLibrary.Game> list = this.visibleGames();
            if (x >= this.menuX && x <= this.menuRight && y >= this.gameTop && y < this.gameTop + (float)this.gameSlots * this.gameStep && (choice = this.gameOffset + (int)((y - this.gameTop) / this.gameStep)) < list.size()) {
                if (choice == this.gameIndex) {
                    this.libraryPlay();
                } else {
                    this.gameIndex = choice;
                    this.sound(this.navSound);
                    this.invalidate();
                }
            }
            return true;
        }

        Dashboard() {
            super((Context)MainActivity.this);
            this.names = new String[]{"Xbox Live", "Games", "Media", "System"};
            this.rows = new String[][]{{"Messages", "Friends", "Marketplace"}, {"Achievements", "Played Games", "Xbox Live Arcade", "Demos", "Trailers"}, {"Music", "Pictures", "Video", "Media Center"}, {"Console Settings", "Family Settings", "Memory", "Network Settings", "Computers", "Initial Setup"}};
            this.topColors = new int[]{-8105679, -12419780, -13537861, -10332224};
            this.midColors = new int[]{-4614046, -9321885, -11756289, -7173893};
            this.bottomColors = new int[]{-3948872, -6434136, -6828549, -5856532};
            this.headerColors = new int[]{-9946841, -13869527, -15256947, -13490288};
            this.p = new Paint(3);
            this.regular = Typeface.createFromAsset((AssetManager)MainActivity.this.getAssets(), (String)"convection.ttf");
            this.bold = Typeface.createFromAsset((AssetManager)MainActivity.this.getAssets(), (String)"convection_bold.ttf");
            this.bladeLeft = new float[]{95.0f, 160.0f, 203.0f, 243.0f};
            this.bladeRight = new float[]{1095.0f, 1152.0f, 1208.0f, 1240.0f};
            this.cardX = new float[]{142.0f, 210.0f, 249.0f, 0.0f};
            this.cardY = new float[]{70.0f, 83.0f, 75.0f, 0.0f};
            this.menuLeft = new float[]{150.0f, 220.0f, 263.0f, 285.0f};
            this.menuEnd = new float[]{581.0f, 648.0f, 690.0f, 728.0f};
            this.menuTop = new float[]{235.0f, 245.0f, 242.0f, 92.0f};
            this.descriptionLeft = new float[]{612.0f, 678.0f, 721.0f, 765.0f};
            this.titleX = new float[]{148.0f, 214.0f, 253.0f, 291.0f};
            this.titleY = new float[]{46.0f, 60.0f, 54.0f, 59.0f};
            this.blade = 1;
            this.row = -1;
            this.title = "";
            this.message = "";
            this.gameTop = 154.0f;
            this.gameStep = 54.0f;
            this.gameSlots = 7;
            this.stickRepeat = new Runnable(){

                @Override
                public void run() {
                    if (Dashboard.this.stickX != 0 || Dashboard.this.stickY != 0) {
                        Dashboard.this.controllerStep(Dashboard.this.stickX, Dashboard.this.stickY);
                        Dashboard.this.postDelayed(this, 160L);
                    }
                }
            };
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);
            this.sounds = new SoundPool.Builder().setMaxStreams(4).build();
            this.navSound = this.sounds.load((Context)MainActivity.this, R.raw.blades_navigation, 1);
            this.bladeSound = this.sounds.load((Context)MainActivity.this, R.raw.blades_tab_transition_01, 1);
            this.selectSound = this.sounds.load((Context)MainActivity.this, R.raw.blades_default_activation, 1);
            this.backSound = this.sounds.load((Context)MainActivity.this, R.raw.blades_hide_modal, 1);
        }

        void sound(int id) {
            if(!library.prefs.getBoolean("sound_enabled",true))return;float volume=library.prefs.getInt("sound_volume",100)/100f;this.sounds.play(id, volume, volume, 1, 0, 1.0f);
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.sounds.release();
        }

        void fill(int color) {
            this.p.reset();
            this.p.setAntiAlias(true);
            this.p.setColor(color);
            this.p.setStyle(Paint.Style.FILL);
        }

        void rect(Canvas c, float l, float t, float r, float b, int color) {
            this.fill(color);
            c.drawRect(l, t, r, b, this.p);
        }

        void round(Canvas c, float l, float t, float r, float b, float rad, int color) {
            this.fill(color);
            c.drawRoundRect(l, t, r, b, rad, rad, this.p);
        }

        void gradient(Canvas c, float l, float t, float r, float b, int a, int z, boolean vertical, float radius) {
            this.fill(a);
            this.p.setShader((Shader)new LinearGradient(l, t, vertical ? l : r, vertical ? b : t, a, z, Shader.TileMode.CLAMP));
            c.drawRoundRect(l, t, r, b, radius, radius, this.p);
            this.p.setShader(null);
        }

        void line(Canvas c, float x1, float y1, float x2, float y2, int color, float width) {
            this.fill(color);
            this.p.setStrokeWidth(width);
            c.drawLine(x1, y1, x2, y2, this.p);
        }

        void text(Canvas c, String text, float x, float y, float size, int color, boolean isBold, boolean shadow) {
            this.fill(color);
            this.p.setTypeface(isBold ? this.bold : this.regular);
            this.p.setTextSize(size);
            if (shadow) {
                this.p.setShadowLayer(2.5f, 1.4f, 1.4f, -1442840576);
            }
            c.drawText(text, x, y, this.p);
            this.p.clearShadowLayer();
        }

        void wrapped(Canvas c, String s, float x, float y, float width, float size, int color) {
            String line = "";
            this.p.setTypeface(this.regular);
            this.p.setTextSize(size);
            for (String word : s.split(" ")) {
                String next;
                String string = next = line.isEmpty() ? word : line + " " + word;
                if (this.p.measureText(next) > width && !line.isEmpty()) {
                    this.text(c, line, x, y, size, color, false, true);
                    y += size * 1.28f;
                    line = word;
                    continue;
                }
                line = next;
            }
            if (!line.isEmpty()) {
                this.text(c, line, x, y, size, color, false, true);
            }
        }

        void glow(Canvas c, float x, float y, float radius, int color) {
            this.fill(color);
            this.p.setShader((Shader)new RadialGradient(x, y, radius, new int[]{color, color & 0xFFFFFF}, null, Shader.TileMode.CLAMP));
            c.drawRect(this.left, 70.0f, this.right, this.H - 80.0f, this.p);
            this.p.setShader(null);
        }

        protected void onDraw(Canvas raw2) {
            int i;
            super.onDraw(raw2);
            this.scale = Math.min((float)this.getWidth() / 1280.0f, (float)this.getHeight() / 720.0f);
            this.W = 1280.0f;
            this.H = (float)this.getHeight() / this.scale;
            this.offsetX = ((float)this.getWidth() - this.W * this.scale) / 2.0f;
            this.fill(this.topColors[this.blade]);
            this.p.setShader((Shader)new LinearGradient(0.0f, 0.0f, 0.0f, (float)this.getHeight(), this.topColors[this.blade], this.bottomColors[this.blade], Shader.TileMode.CLAMP));
            raw2.drawRect(0.0f, 0.0f, (float)this.getWidth(), (float)this.getHeight(), this.p);
            this.p.setShader(null);
            Canvas c = raw2;
            c.save();
            c.translate(this.offsetX, 0.0f);
            c.scale(this.scale, this.scale);
            this.gradient(c, 0.0f, 0.0f, this.W, this.H, this.topColors[this.blade], this.bottomColors[this.blade], true, 0.0f);
            float tab = 57.0f;
            this.left = this.bladeLeft[this.blade];
            this.right = this.bladeRight[this.blade];
            for (i = 3; i > this.blade; --i) {
                this.drawTab(c, this.right + (float)(i - this.blade - 1) * tab, i, tab);
            }
            for (i = 0; i < this.blade; ++i) {
                this.drawTab(c, this.left - (float)(this.blade - i) * tab - 30.0f, i, tab);
            }
            Path active = this.activeSheet();
            c.save();
            c.clipPath(active);
            this.gradient(c, this.left - 60.0f, 0.0f, this.right + 10.0f, this.H, this.topColors[this.blade], this.bottomColors[this.blade], true, 0.0f);
            float innerL = this.left + 42.0f;
            float innerR = this.right - 20.0f;
            float innerW = innerR - innerL;
            this.gradient(c, innerL, 52.0f, innerR, this.H - 80.0f, 0x22000000, 0x11FFFFFF, true, 8.0f);
            this.glow(c, innerL + innerW * 0.66f, Math.min(this.H - 180.0f, 320.0f + (this.H - 720.0f) * 0.25f), innerW * 0.72f, this.blade == 1 ? 1719664545 : (this.blade == 2 ? 1720965631 : (this.blade == 0 ? 1728045984 : 1725936895)));
            this.gradient(c, innerL, 72.0f, innerL + 54.0f, this.H - 100.0f, 1426856723, 793363, false, 0.0f);
            this.gradient(c, innerR - 55.0f, 72.0f, innerR, this.H - 100.0f, 1386261, 1427449621, false, 0.0f);
            this.gradient(c, this.left + 20.0f, -35.0f, this.right, 72.0f, -15656425, this.headerColors[this.blade], true, 28.0f);
            if (media != null && media.active) {
                media.draw(c);
            } else if (this.libraryMode) {
                this.drawLibrary(c);
            } else {
                this.text(c, this.names[this.blade], this.titleX[this.blade], this.titleY[this.blade], 37.0f, -722449, false, true);
                this.menuX = this.menuLeft[this.blade];
                this.menuRight = this.menuEnd[this.blade];
                this.rowStep = 53.0f;
                float descriptionX = this.descriptionLeft[this.blade];
                if (this.blade != 3) {
                    this.drawCard(c, this.cardX[this.blade], this.cardY[this.blade], 538.0f, 150.0f);
                    this.drawXboxLogo(c, this.blade == 0 ? 815.0f : (this.blade == 1 ? 887.0f : 931.0f), this.blade == 0 ? 137.0f : (this.blade == 1 ? 147.0f : 138.0f), this.blade == 0);
                    this.rowY = this.menuTop[this.blade];
                    this.text(c, this.blade==2?"Your media":"Xbox Live", descriptionX, this.rowY + 38.0f, 30.0f, -15194084, false, true);
                    this.wrapped(c, this.blade==2?"Play music from your chosen folder. View this app\'s screenshots and recordings in the media gallery.":"Games. Tournaments. Entertainment. All the rewards. Endless possibilities. What are you waiting for?", descriptionX, this.rowY + 77.0f, innerR - descriptionX - 20.0f, 27.0f, -15456229);
                } else {
                    this.rowY = this.menuTop[this.blade];
                    this.wrapped(c, "Edit your Xbox 360 system settings, including:", descriptionX, 120.0f, innerR - descriptionX - 25.0f, 27.0f, -14475991);
                    String[] bullets = new String[]{"Display", "Audio", "Language", "Remote control", "and more"};
                    for (int i2 = 0; i2 < bullets.length; ++i2) {
                        this.text(c, "\u2022 " + bullets[i2], descriptionX, 226 + i2 * 35, 27.0f, -14475991, false, true);
                    }
                }
                for (int i3 = 0; i3 < this.rows[this.blade].length; ++i3) {
                    this.drawRow(c, i3);
                }
                if (this.blade == 0) {
                    this.gradient(c, this.menuX, this.rowY + (float)this.rows[this.blade].length * this.rowStep + 16.0f, this.menuRight, this.rowY + (float)this.rows[this.blade].length * this.rowStep + 94.0f, -12362364, -14337957, true, 10.0f);
                    this.text(c, "XBOX", this.menuX + 130.0f, this.rowY + (float)this.rows[this.blade].length * this.rowStep + 48.0f, 28.0f, -4069722, true, true);
                    this.text(c, "LIVE", this.menuX + 143.0f, this.rowY + (float)this.rows[this.blade].length * this.rowStep + 78.0f, 36.0f, -13939, true, true);
                }
                float tray = this.H - 175.0f;
                if (this.blade != 3) {
                    this.gradient(c, innerL + 7.0f, tray, innerR - 7.0f, tray + 51.0f, 1149545849, 0x33FFFFFF, true, 28.0f);
                    this.fill(1382254950);
                    this.p.setStyle(Paint.Style.STROKE);
                    this.p.setStrokeWidth(2.0f);
                    c.drawRoundRect(innerL + 7.0f, tray, innerR - 7.0f, tray + 51.0f, 27.0f, 27.0f, this.p);
                    c.drawCircle(innerL + 45.0f, tray + 25.0f, 35.0f, this.p);
                    this.p.setStyle(Paint.Style.FILL);
                    this.text(c, "\u25c0", innerL + 35.0f, tray + 33.0f, 17.0f, -10254493, false, false);
                    drawLoadedTray(c,innerL,innerR,tray);
                }
                this.gradient(c, this.left + 20.0f, this.H - 96.0f, this.right, this.H + 20.0f, 0x66FFFFFF, 1148223322, true, 24.0f);
                this.button(c, innerL + 20.0f, this.H - 39.0f, "X", -14647870);
                this.text(c, "Sign Out", innerL + 42.0f, this.H - 31.0f, 24.0f, -920343, false, true);
                this.button(c, innerR - 55.0f, this.H - 39.0f, "A", -8145090);
                this.text(c, "Select", innerR - 134.0f, this.H - 31.0f, 24.0f, -920343, false, true);
            }
            c.restore();
            this.fill(-1725944794);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(2.0f);
            c.drawPath(active, this.p);
            this.p.setStyle(Paint.Style.FILL);
            this.bladeLabel(c, this.names[this.blade], this.left - 22.0f, 125.0f, 29.0f);
            for (int i4 = 0; i4 < 4; ++i4) {
                if (i4 == this.blade) continue;
                float x = i4 < this.blade ? this.left - (float)(this.blade - i4) * tab - 30.0f : this.right + (float)(i4 - this.blade - 1) * tab;
                this.bladeLabel(c, this.names[i4], i4 < this.blade ? x + 5.0f : x + tab - 5.0f, 145.0f, 28.0f);
            }
            if (this.dialog) {
                this.drawDialog(c);
            }
            if(optionMode!=0)drawOptions(c);
            if(media!=null)media.drawOverlay(c);
            if(tools!=null)tools.draw(c);
            if(profile!=null)profile.preview(c);
            if(media!=null && media.recording())text(c,"REC  â€¢  X to stop",W-260,34,20,0xffffdddd,true,true);
            c.restore();
        }

        void drawTab(Canvas c, float x, int index, float tab) {
            c.save();
            if (index < this.blade) {
                c.clipRect(-this.W, 0.0f, this.left, this.H);
            } else {
                c.clipRect(this.right - 20.0f, 0.0f, this.W * 2.0f, this.H);
            }
            if (index > this.blade) {
                c.translate(2.0f * x + tab, 0.0f);
                c.scale(-1.0f, 1.0f);
            }
            Path metal = new Path();
            metal.moveTo(x + 16.0f, 0.0f);
            metal.cubicTo(x + 19.0f, 35.0f, x + 26.0f, 83.0f, x + 21.0f, 96.0f);
            metal.cubicTo(x + 16.0f, 109.0f, x - 15.0f, 100.0f, x - 18.0f, 119.0f);
            metal.cubicTo(x - 14.0f, this.H * 0.34f, x - 13.0f, this.H * 0.52f, x - 14.0f, this.H * 0.65f);
            metal.cubicTo(x - 15.0f, this.H * 0.74f, x + 22.0f, this.H * 0.8f, x + 23.0f, this.H * 0.88f);
            metal.cubicTo(x + 23.0f, this.H * 0.94f, x + 17.0f, this.H * 0.98f, x + 15.0f, this.H);
            metal.lineTo(x + this.W * 2.0f, this.H);
            metal.lineTo(x + this.W * 2.0f, 0.0f);
            metal.close();
            this.fill(-3617833);
            this.p.setShader((Shader)new LinearGradient(x - 20.0f, 0.0f, x + 80.0f, 0.0f, new int[]{-12301483, -4275504, -2499353, -4735797}, new float[]{0.0f, 0.08f, 0.55f, 1.0f}, Shader.TileMode.CLAMP));
            c.drawPath(metal, this.p);
            this.p.setShader(null);
            this.fill(-1438232229);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(2.0f);
            c.drawPath(metal, this.p);
            this.p.setStyle(Paint.Style.FILL);
            c.restore();
        }

        Path activeSheet() {
            Path sheet = new Path();
            sheet.moveTo(this.left - 15.0f, 0.0f);
            sheet.cubicTo(this.left - 12.0f, 28.0f, this.left - 3.0f, 67.0f, this.left - 5.0f, 82.0f);
            sheet.cubicTo(this.left - 7.0f, 99.0f, this.left - 46.0f, 89.0f, this.left - 49.0f, 106.0f);
            sheet.cubicTo(this.left - 44.0f, this.H * 0.32f, this.left - 44.0f, this.H * 0.51f, this.left - 45.0f, this.H * 0.66f);
            sheet.cubicTo(this.left - 46.0f, this.H * 0.75f, this.left - 4.0f, this.H * 0.82f, this.left - 1.0f, this.H * 0.88f);
            sheet.cubicTo(this.left + 1.0f, this.H * 0.94f, this.left - 4.0f, this.H * 0.98f, this.left - 5.0f, this.H);
            sheet.lineTo(this.right, this.H);
            sheet.cubicTo(this.right - 14.0f, this.H * 0.8f, this.right - 18.0f, this.H * 0.48f, this.right - 9.0f, this.H * 0.24f);
            sheet.cubicTo(this.right - 5.0f, this.H * 0.12f, this.right, this.H * 0.05f, this.right + 3.0f, 0.0f);
            sheet.close();
            return sheet;
        }

        void drawActiveSpine(Canvas c) {
            float x = this.left;
            Path spine = new Path();
            spine.moveTo(x - 29.0f, 0.0f);
            spine.lineTo(x + 23.0f, 0.0f);
            spine.cubicTo(x + 19.0f, 44.0f, x + 30.0f, 91.0f, x + 21.0f, 108.0f);
            spine.cubicTo(x + 16.0f, 124.0f, x - 1.0f, 113.0f, x - 2.0f, 146.0f);
            spine.cubicTo(x - 1.0f, this.H * 0.37f, x + 2.0f, this.H * 0.61f, x + 5.0f, this.H * 0.69f);
            spine.cubicTo(x + 8.0f, this.H * 0.78f, x + 30.0f, this.H * 0.82f, x + 32.0f, this.H);
            spine.lineTo(x - 22.0f, this.H);
            spine.cubicTo(x - 22.0f, this.H * 0.84f, x - 35.0f, this.H * 0.78f, x - 38.0f, this.H * 0.68f);
            spine.cubicTo(x - 42.0f, this.H * 0.49f, x - 34.0f, this.H * 0.3f, x - 37.0f, 145.0f);
            spine.cubicTo(x - 38.0f, 122.0f, x - 43.0f, 117.0f, x - 42.0f, 104.0f);
            spine.cubicTo(x - 40.0f, 65.0f, x - 33.0f, 27.0f, x - 29.0f, 0.0f);
            spine.close();
            this.fill(this.midColors[this.blade]);
            this.p.setShader((Shader)new LinearGradient(x - 42.0f, 0.0f, x + 35.0f, 0.0f, new int[]{-14140628, this.headerColors[this.blade], this.midColors[this.blade], -2757185, this.headerColors[this.blade]}, new float[]{0.0f, 0.22f, 0.53f, 0.76f, 1.0f}, Shader.TileMode.CLAMP));
            c.drawPath(spine, this.p);
            this.p.setShader(null);
            this.fill(-1441323750);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(3.0f);
            c.drawPath(spine, this.p);
            this.p.setStyle(Paint.Style.FILL);
            c.save();
            c.clipPath(spine);
            Path shoulder = new Path();
            shoulder.moveTo(x - 45.0f, 0.0f);
            shoulder.lineTo(x + 1.0f, 0.0f);
            shoulder.cubicTo(x + 3.0f, 32.0f, x + 8.0f, 63.0f, x + 4.0f, 78.0f);
            shoulder.cubicTo(x + 2.0f, 91.0f, x - 17.0f, 88.0f, x - 27.0f, 94.0f);
            shoulder.cubicTo(x - 34.0f, 99.0f, x - 34.0f, 114.0f, x - 34.0f, 134.0f);
            shoulder.lineTo(x - 48.0f, 134.0f);
            shoulder.close();
            this.fill(-3552296);
            this.p.setShader((Shader)new LinearGradient(x - 43.0f, 0.0f, x + 5.0f, 0.0f, -7498076, -1381389, Shader.TileMode.CLAMP));
            c.drawPath(shoulder, this.p);
            this.p.setShader(null);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(1.6f);
            this.p.setColor(-9866626);
            c.drawPath(shoulder, this.p);
            this.p.setStyle(Paint.Style.FILL);
            c.restore();
            this.bladeLabel(c, this.names[this.blade], x - 20.0f, 145.0f, 29.0f);
        }

        void bladeLabel(Canvas c, String label, float centerX, float top, float size) {
            this.p.setTypeface(this.regular);
            this.p.setTextSize(size);
            Paint.FontMetrics metrics = this.p.getFontMetrics();
            float baselineX = centerX + (metrics.ascent + metrics.descent) / 2.0f;
            c.save();
            c.translate(baselineX, top);
            c.rotate(90.0f);
            this.text(c, label.toLowerCase(Locale.ROOT), 0.0f, 0.0f, size, -14670550, false, false);
            c.restore();
        }

        void drawCard(Canvas c, float x, float y, float w, float h) {
            this.round(c, x, y, x + w, y + h, 9.0f, 1430401064);
            this.gradient(c, x, y, x + w, y + 35.0f, -301326572, -870633440, true, 7.0f);
            this.text(c, this.fit(profile.name(),w-32,27), x + 16.0f, y + 27.0f, 27.0f, -1445407, false, true);
            this.gradient(c, x + 13.0f, y + 47.0f, x + 95.0f, y + 130.0f, -2692991, -11043009, true, 0.0f);
            c.save();
            c.clipRect(x + 13.0f, y + 47.0f, x + 95.0f, y + 130.0f);
            for (int i = 0; i < 5; ++i) {
                this.fill(1441791930);
                this.p.setStyle(Paint.Style.STROKE);
                this.p.setStrokeWidth(2.0f);
                c.drawCircle(x + 54.0f, y + 49.0f, (float)(12 + i * 9), this.p);
                this.p.setStyle(Paint.Style.FILL);
            }
            c.restore();
            profile.draw(c,new RectF(x+13,y+47,x+95,y+130),false);
            this.text(c, "Games", x + 108.0f, y + 68.0f, 26.0f, -15456487, false, true);
            this.text(c, "Gamerscore", x + 108.0f, y + 99.0f, 26.0f, -15456487, false, true);
            this.text(c, "Achievements", x + 108.0f, y + 129.0f, 26.0f, -15456487, false, true);
            String quantity=String.valueOf(library.playableCount());this.p.setTypeface(this.regular);this.p.setTextSize(24);this.text(c,quantity,x+w-20-this.p.measureText(quantity),y+68,24,-14271191,false,true);
            String score=retroAchievements.score(),unlocks=retroAchievements.achievements();this.p.setTypeface(this.regular);this.p.setTextSize(24);this.text(c,score,x+w-20-this.p.measureText(score),y+99,24,-14271191,false,true);this.p.setTypeface(this.regular);this.p.setTextSize(24);this.text(c,unlocks,x+w-20-this.p.measureText(unlocks),y+129,24,-14271191,false,true);
        }

        void drawXboxLogo(Canvas c, float x, float y, boolean live) {
            if (live) {
                this.fill(-5907836);
                this.p.setTypeface(this.regular);
                this.p.setTextAlign(Paint.Align.CENTER);
                this.p.setTextSize(42.0f);
                this.p.setShadowLayer(3.0f, 1.0f, 2.0f, -1157627904);
                c.drawText("XBOX", x, y - 4.0f, this.p);
                this.p.setTextSize(53.0f);
                this.p.setColor(-2118024);
                c.drawText("LIVE", x, y + 44.0f, this.p);
                this.p.clearShadowLayer();
                this.p.setTextAlign(Paint.Align.LEFT);
                return;
            }
            this.fill(-14929885);
            this.p.setShadowLayer(5.0f, 1.0f, 3.0f, -1442840576);
            c.drawCircle(x, y - 15.0f, 43.0f, this.p);
            this.p.clearShadowLayer();
            this.fill(-2563619);
            this.p.setShader((Shader)new RadialGradient(x - 15.0f, y - 34.0f, 76.0f, new int[]{-327942, -5062468, -10456469, -14272463}, null, Shader.TileMode.CLAMP));
            c.drawCircle(x, y - 15.0f, 39.0f, this.p);
            this.p.setShader(null);
            c.save();
            Path clip = new Path();
            clip.addCircle(x, y - 15.0f, 37.0f, Path.Direction.CW);
            c.clipPath(clip);
            this.fill(-8930250);
            this.p.setShader((Shader)new LinearGradient(x - 36.0f, y - 52.0f, x + 30.0f, y + 20.0f, -3017602, -11756767, Shader.TileMode.CLAMP));
            Path mark = new Path();
            mark.moveTo(x - 40.0f, y - 53.0f);
            mark.cubicTo(x - 18.0f, y - 49.0f, x - 7.0f, y - 42.0f, x, y - 35.0f);
            mark.cubicTo(x + 8.0f, y - 42.0f, x + 20.0f, y - 49.0f, x + 40.0f, y - 53.0f);
            mark.cubicTo(x + 23.0f, y - 41.0f, x + 12.0f, y - 29.0f, x + 6.0f, y - 20.0f);
            mark.cubicTo(x + 18.0f, y - 8.0f, x + 26.0f, y + 8.0f, x + 27.0f, y + 22.0f);
            mark.cubicTo(x + 15.0f, y + 7.0f, x + 8.0f, y - 3.0f, x, y - 10.0f);
            mark.cubicTo(x - 8.0f, y - 3.0f, x - 16.0f, y + 7.0f, x - 27.0f, y + 22.0f);
            mark.cubicTo(x - 25.0f, y + 7.0f, x - 18.0f, y - 8.0f, x - 6.0f, y - 20.0f);
            mark.cubicTo(x - 12.0f, y - 29.0f, x - 23.0f, y - 41.0f, x - 40.0f, y - 53.0f);
            mark.close();
            c.drawPath(mark, this.p);
            this.p.setShader(null);
            c.restore();
            String logo = live ? "XBOX LIVE" : "XBOX 360";
            this.fill(-1773613);
            this.p.setTypeface(this.regular);
            this.p.setTextSize(37.0f);
            this.p.setTextAlign(Paint.Align.CENTER);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(3.0f);
            this.p.setColor(-14206425);
            c.drawText(logo, x, y + 65.0f, this.p);
            this.p.setStyle(Paint.Style.FILL);
            this.p.setColor(-5713021);
            c.drawText(logo, x, y + 65.0f, this.p);
            this.p.setTextAlign(Paint.Align.LEFT);
        }

        void drawRow(Canvas c, int i) {
            float y = this.rowY + (float)i * this.rowStep;
            if (i == this.row) {
                this.gradient(c, this.menuX - 3.0f, y - 2.0f, this.menuRight, y + 47.0f, -1711734798, 1720235643, true, 8.0f);
            }
            this.line(c, this.menuX, y + 48.0f, this.menuRight, y + 48.0f, 1715821896, 2.0f);
            this.drawIcon(c, this.menuX + 21.0f, y + 22.0f, i);
            this.text(c, this.rows[this.blade][i], this.menuX + 62.0f, y + 35.0f, 29.0f, -15719912, false, true);
        }

        void drawIcon(Canvas c, float x, float y, int i) {
            this.fill(-1708828);
            this.p.setShadowLayer(2.0f, 1.0f, 2.0f, -1728053248);
            if (this.blade == 0 && i == 0) {
                Path a = new Path();
                a.moveTo(x - 15.0f, y - 10.0f);
                a.lineTo(x + 15.0f, y - 10.0f);
                a.lineTo(x + 15.0f, y + 10.0f);
                a.lineTo(x - 15.0f, y + 10.0f);
                a.close();
                c.drawPath(a, this.p);
                this.p.clearShadowLayer();
                this.line(c, x - 14.0f, y - 9.0f, x, y + 2.0f, -9272709, 2.0f);
                this.line(c, x, y + 2.0f, x + 14.0f, y - 9.0f, -9272709, 2.0f);
                return;
            }
            if (this.blade == 1 && i == 0) {
                c.drawRoundRect(x - 10.0f, y - 14.0f, x + 10.0f, y + 6.0f, 5.0f, 5.0f, this.p);
                c.drawRect(x - 3.0f, y + 4.0f, x + 3.0f, y + 13.0f, this.p);
                c.drawRect(x - 11.0f, y + 12.0f, x + 11.0f, y + 16.0f, this.p);
            } else if (this.blade == 1 && i == 1) {
                Path pad = new Path();
                pad.moveTo(x - 13.0f, y - 11.0f);
                pad.cubicTo(x - 20.0f, y - 9.0f, x - 20.0f, y + 10.0f, x - 15.0f, y + 13.0f);
                pad.lineTo(x - 7.0f, y + 7.0f);
                pad.lineTo(x + 7.0f, y + 7.0f);
                pad.lineTo(x + 15.0f, y + 13.0f);
                pad.cubicTo(x + 20.0f, y + 10.0f, x + 20.0f, y - 9.0f, x + 13.0f, y - 11.0f);
                pad.close();
                c.drawPath(pad, this.p);
                this.fill(-9140870);
                c.drawRect(x - 11.0f, y - 5.0f, x - 3.0f, y - 2.0f, this.p);
                c.drawRect(x - 8.0f, y - 8.0f, x - 6.0f, y + 1.0f, this.p);
                c.drawCircle(x + 8.0f, y - 3.0f, 2.0f, this.p);
            } else if (this.blade == 1 && i == 2) {
                c.save();
                c.rotate(-14.0f, x, y);
                c.drawRoundRect(x - 4.0f, y - 12.0f, x + 4.0f, y + 8.0f, 3.0f, 3.0f, this.p);
                c.drawCircle(x, y - 13.0f, 5.0f, this.p);
                c.drawRoundRect(x - 15.0f, y + 8.0f, x + 15.0f, y + 15.0f, 2.0f, 2.0f, this.p);
                c.restore();
            } else if (this.blade == 1 && i == 3 || this.blade == 2 && i == 0) {
                c.drawCircle(x, y, 15.0f, this.p);
                this.fill(-8414583);
                c.drawCircle(x, y, 4.0f, this.p);
            } else if (this.blade == 1 && i == 4) {
                c.save();
                c.rotate(-10.0f, x, y);
                c.drawRoundRect(x - 16.0f, y - 12.0f, x + 16.0f, y + 11.0f, 2.0f, 2.0f, this.p);
                this.fill(-8219254);
                c.drawRect(x - 11.0f, y - 7.0f, x + 10.0f, y + 5.0f, this.p);
                for (int k = -1; k <= 1; ++k) {
                    c.drawRect(x - 15.0f, y + (float)(k * 7) - 1.0f, x - 12.0f, y + (float)(k * 7) + 2.0f, this.p);
                    c.drawRect(x + 12.0f, y + (float)(k * 7) - 1.0f, x + 15.0f, y + (float)(k * 7) + 2.0f, this.p);
                }
                c.restore();
            } else if (this.blade == 2 && i == 1) {
                c.drawRoundRect(x - 16.0f, y - 12.0f, x + 16.0f, y + 11.0f, 3.0f, 3.0f, this.p);
                this.fill(-8414583);
                c.drawCircle(x, y - 1.0f, 5.0f, this.p);
            } else {
                c.drawCircle(x, y - 7.0f, 7.0f, this.p);
                c.drawRoundRect(x - 10.0f, y + 2.0f, x + 10.0f, y + 13.0f, 6.0f, 6.0f, this.p);
            }
            this.p.clearShadowLayer();
        }

        void button(Canvas c, float x, float y, String name, int color) {
            this.fill(color);
            this.p.setShadowLayer(3.0f, 0.0f, 1.0f, -1442840576);
            c.drawCircle(x, y, 15.0f, this.p);
            this.p.clearShadowLayer();
            this.text(c, name, x - 6.0f, y + 7.0f, 19.0f, -1, true, false);
        }



    void openSystem(int mode){if(mode==15){profile.start();return;}optionMode=mode;optionIndex=optionOffset=0;stopStick();if(mode==14)optionApps=library.apps(false);requestFocus();invalidate();}
    String onOff(String key,boolean fallback){return library.prefs.getBoolean(key,fallback)?"On":"Off";}
    void toggle(String key,boolean fallback){library.prefs.edit().putBoolean(key,!library.prefs.getBoolean(key,fallback)).apply();}
    String systemTitle(){if(optionMode==17)return "Launch game";return rows[3][Math.max(0,Math.min(5,optionMode-10))];}
    String systemSubtitle(){switch(optionMode){case 10:return "Frontend sound and controller";case 11:return "Frontend launch confirmation";case 12:return library.games.size()+" games in your library";case 13:return vpn.status;case 14:return "Installed emulators";case 15:return "Add your games";default:return optionGame==null?"":EmulatorLibrary.cleanTitle(optionGame.name);}}
    String[] systemLabels(){switch(optionMode){
      case 10:return new String[]{"Menu sounds: "+onOff("sound_enabled",true),"Sound volume: "+library.prefs.getInt("sound_volume",100)+"%","Stick dead zone: "+library.prefs.getInt("deadzone",35)+"%","Gamer Profile","Initial Setup"};
      case 11:return new String[]{"Confirm game launch: "+onOff("confirm_launch",false),"Removal always requires confirmation"};
      case 12:return new String[]{"Open game library","Scan a ROM folder","Refresh local artwork","Clear downloaded cover cache","Library tools","Backup & restore"};
      case 13:return new String[]{vpn.label(),"PIA VPN settings","Check public IP / location","Automatic cover downloads: "+onOff("auto_art",true),"Retry missing artwork"};
      case 14:{String[] labels=new String[optionApps.size()];for(int i=0;i<labels.length;i++)labels[i]=optionApps.get(i).name;return labels.length==0?new String[]{"No supported emulators installed"}:labels;}
      case 15:return new String[]{"Choose ROM files","Scan a ROM folder","Open game library"};
      default:return new String[]{"Cancel","Launch game"};
    }}
    void pickGames(boolean folder){stopStick();Intent i=folder?new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE):new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);try{startActivityForResult(i,folder?EmulatorLibrary.FOLDER:EmulatorLibrary.IMPORT);}catch(Exception e){library.toast("No document picker is installed.");}}
    void activateSystem(){switch(optionMode){
      case 10:if(optionIndex==0)toggle("sound_enabled",true);else if(optionIndex==1)library.prefs.edit().putInt("sound_volume",(library.prefs.getInt("sound_volume",100)+25)%125).apply();else if(optionIndex==2)library.prefs.edit().putInt("deadzone",library.prefs.getInt("deadzone",35)>=50?20:library.prefs.getInt("deadzone",35)+5).apply();else if(optionIndex==3)profile.openProfile();else profile.start();break;
      case 11:if(optionIndex==0)toggle("confirm_launch",false);break;
      case 12:if(optionIndex==0){optionMode=0;library.open();}else if(optionIndex==1)pickGames(true);else if(optionIndex==2){library.artScanned=false;library.scanArtwork();}else if(optionIndex==3){library.clearCoverCache();}else{optionMode=0;if(optionIndex==4)tools.openHome();else tools.openBackup();}break;
      case 13:if(optionIndex==0)vpn.toggle(MainActivity.this);else if(optionIndex==1)vpn.openSettings(MainActivity.this);else if(optionIndex==2){vpn.check();vpn.details(MainActivity.this);}else if(optionIndex==3)toggle("auto_art",true);else{library.remoteTried.clear();library.remoteFailures.clear();library.toast("Missing artwork will be retried as you browse.");}break;
      case 14:if(!optionApps.isEmpty()){Intent i=getPackageManager().getLaunchIntentForPackage(optionApps.get(optionIndex).pkg);try{if(i!=null)startActivity(i);else library.toast("Emulator unavailable.");}catch(Exception e){library.toast("Emulator unavailable.");}}break;
      case 15:if(optionIndex<2)pickGames(optionIndex==1);else{optionMode=0;library.open();}break;
      case 17:optionMode=0;if(optionIndex==1)library.launchConfirmed(optionGame);break;
    }}

    int optionMode,optionIndex,optionOffset,optionSlots=6;
    boolean optionPlay,optionAllApps;
    EmulatorLibrary.Game optionGame;
    java.util.ArrayList<EmulatorLibrary.App> optionApps=new java.util.ArrayList<>();
    float optionTop=188,optionStep=49;
    void openOptions(EmulatorLibrary.Game game){optionGame=game;optionMode=1;optionIndex=optionOffset=0;stopStick();requestFocus();sound(selectSound);invalidate();}
    void openEmulators(EmulatorLibrary.Game game,boolean play,boolean all){optionGame=game;optionPlay=play;optionAllApps=all;optionApps=library.apps(all);optionApps.add(0,new EmulatorLibrary.App("","Automatic / core or installed emulator"));optionMode=2;optionIndex=optionOffset=0;stopStick();requestFocus();invalidate();}
    String[] optionLabels(){
      if(optionMode>=10)return systemLabels();
      if(optionMode==3)return new String[]{"Cancel","Remove from library"};
      if(optionMode==2){String[] labels=new String[optionApps.size()+1];for(int i=0;i<optionApps.size();i++)labels[i]=optionApps.get(i).name;labels[labels.length-1]=optionAllApps?"Recognized emulators":"All installed applications";return labels;}
      return new String[]{"Play",optionGame.emulator.isEmpty()?"Choose game emulator":"Change game emulator",optionGame.favorite?"Remove favorite":"Add favorite","Choose artwork","Refresh local artwork","Remove from library","Updates & DLC ("+library.extras(optionGame).size()+")","Search artwork","Set console emulator default","Use console default","Library tools"};
    }
    void moveOption(int delta){String[] labels=optionLabels();optionIndex=Math.max(0,Math.min(labels.length-1,optionIndex+delta));if(optionIndex<optionOffset)optionOffset=optionIndex;if(optionIndex>=optionOffset+optionSlots)optionOffset=optionIndex-optionSlots+1;sound(navSound);invalidate();}
    void drawOptions(Canvas c){
      rect(c,-offsetX/scale,0,W+offsetX/scale,H,0xa6000000);
      float edge=572;
      Path panel=new Path();panel.moveTo(-offsetX/scale,0);panel.lineTo(edge-13,0);panel.cubicTo(edge-22,45,edge-34,82,edge-29,96);panel.cubicTo(edge-25,108,edge+14,93,edge+13,119);panel.cubicTo(edge+10,H*.38f,edge+5,H*.60f,edge-2,H*.68f);panel.cubicTo(edge-18,H*.78f,edge-32,H*.82f,edge-20,H);panel.lineTo(-offsetX/scale,H);panel.close();
      fill(0xffced7c4);p.setShadowLayer(14,10,0,0xbb000000);c.drawPath(panel,p);p.clearShadowLayer();
      c.save();c.clipPath(panel);gradient(c,-40,0,edge+20,H,0xffdce9cc,0xff839d66,true,0);gradient(c,edge-52,0,edge+20,H,0xffbac6b2,0xff4a5f42,false,0);gradient(c,-40,-25,edge+2,91,0xff415d28,0xff91b950,true,24);gradient(c,-40,H-100,edge+2,H+25,0xff5b783d,0xffb8ce99,true,24);
      text(c,optionMode>=10?systemTitle():optionMode==2?"Choose emulator":optionMode==3?"Remove game":"Game options",45,56,33,0xfff6f8e9,false,true);
      round(c,40,108,515,163,7,0x77617947);text(c,fit(optionMode>=10?systemSubtitle():EmulatorLibrary.cleanTitle(optionGame.name),435,25),55,143,25,0xff20321b,false,false);
      if(optionMode==3){wrapped(c,"Remove this entry? Your ROM file will stay on the device.",48,205,450,24,0xff273b21);optionTop=285;}else optionTop=188;
      optionSlots=Math.max(2,Math.min(8,(int)((H-135-optionTop)/optionStep)));
      String[] labels=optionLabels();optionIndex=Math.min(optionIndex,labels.length-1);if(optionIndex<optionOffset)optionOffset=optionIndex;if(optionIndex>=optionOffset+optionSlots)optionOffset=optionIndex-optionSlots+1;
      for(int i=optionOffset;i<Math.min(labels.length,optionOffset+optionSlots);i++){float y=optionTop+(i-optionOffset)*optionStep;if(i==optionIndex)gradient(c,40,y-3,518,y+43,0xfff8fbe8,0xffa9c983,true,9);line(c,45,y+44,514,y+44,0x666a7e53,1.4f);text(c,fit(labels[i],447,26),58,y+31,26,0xff20331b,false,false);}
      if(optionOffset>0)text(c,"â–²",490,optionTop-10,20,0xff3d572c,false,false);if(optionOffset+optionSlots<labels.length)text(c,"â–¼",490,optionTop+optionSlots*optionStep+19,20,0xff3d572c,false,false);
      button(c,64,H-45,"A",0xff82b83c);text(c,"Select",88,H-37,24,0xfff4f6e7,false,true);button(c,376,H-45,"B",0xffbd3a30);text(c,"Back",400,H-37,24,0xfff4f6e7,false,true);
      c.save();c.translate(edge-11,175);c.rotate(90);text(c,optionMode>=10?systemTitle():"Game options",0,0,23,0xff273a20,false,false);c.restore();c.restore();
      fill(0xff56694a);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawPath(panel,p);p.setStyle(Paint.Style.FILL);
    }
    boolean optionsTouch(float x,float y){if(x>590){back();return true;}if(y>H-100){if(x<280)activateOption();else back();return true;}if(x>=40&&x<=520&&y>=optionTop){int choice=optionOffset+(int)((y-optionTop)/optionStep);if(choice<optionLabels().length&&choice<optionOffset+optionSlots){if(choice==optionIndex)activateOption();else{optionIndex=choice;sound(navSound);invalidate();}}}return true;}
    void activateOption(){
      sound(selectSound);stopStick();
      if(optionMode>=10){activateSystem();invalidate();return;}
      if(optionMode==3){if(optionIndex==1){library.games.remove(optionGame);library.save();optionMode=0;clampGames(visibleGames().size());}else{optionMode=1;optionIndex=0;}invalidate();return;}
      if(optionMode==2){if(optionIndex==optionApps.size()){openEmulators(optionGame,optionPlay,!optionAllApps);return;}optionGame.emulator=optionApps.get(optionIndex).pkg;library.save();if(optionPlay){optionMode=0;library.launch(optionGame);}else{optionMode=1;optionIndex=1;}invalidate();return;}
      switch(optionIndex){
        case 0:if(library.effectiveEmulator(optionGame).isEmpty())openEmulators(optionGame,true,false);else{optionMode=0;library.launch(optionGame);}break;
        case 1:openEmulators(optionGame,false,false);break;
        case 2:optionGame.favorite=!optionGame.favorite;library.save();break;
        case 3:library.artGame=optionGame;try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),EmulatorLibrary.ART);}catch(Exception e){library.toast("No image picker is available.");}break;
        case 4:library.artScanned=false;library.scanArtwork();break;
        case 5:optionMode=3;optionIndex=optionOffset=0;break;
        case 6:optionMode=0;tools.openExtras(optionGame);break;
        case 7:optionMode=0;tools.openArtwork(optionGame);break;
        case 8:optionMode=0;tools.openDefault(optionGame.system);break;
        case 9:library.useConsoleDefault(optionGame);break;
        case 10:optionMode=0;tools.openHome();break;
      }
      invalidate();
    }

        void drawDialog(Canvas c) {
            this.rect(c, 0.0f, 0.0f, this.W, this.H, -1728053248);
            float x = this.W / 2.0f - 340.0f;
            float y = this.H / 2.0f - 150.0f;
            this.gradient(c, x, y, x + 680.0f, y + 300.0f, -854033, -5257556, true, 20.0f);
            this.text(c, this.title, x + 35.0f, y + 58.0f, 32.0f, -14469078, true, false);
            this.wrapped(c, this.message, x + 35.0f, y + 115.0f, 610.0f, 25.0f, -14271188);
            this.button(c, x + 48.0f, y + 255.0f, "A", -8344265);
            this.text(c, "OK", x + 72.0f, y + 263.0f, 24.0f, -14271188, false, false);
        }

        void navigate(int d) {
            if (this.libraryMode) {
                this.filterGames(d);
                return;
            }
            int next = Math.max(0, Math.min(3, this.blade + d));
            if (next != this.blade) {
                this.blade = next;
                this.row = -1;
                this.sound(this.bladeSound);
                this.invalidate();
            }
        }

        void playTray(){
            EmulatorLibrary.Game game=MainActivity.this.library.loadedGame();
            stopStick();
            if(game==null){MainActivity.this.library.open();return;}
            if(library.effectiveEmulator(game).isEmpty())MainActivity.this.library.chooseEmulator(game,true);
            else MainActivity.this.library.launch(game);
        }
        void drawLoadedTray(Canvas c,float innerL,float innerR,float y){
            EmulatorLibrary.Game game=MainActivity.this.library.loadedGame();
            if(row==rows[blade].length)gradient(c,innerL+7,y,innerR-7,y+51,0x99f4ffe5,0x5581a06b,true,27);
            if(game!=null){
                fill(0xffdce6de);p.setShader(new LinearGradient(innerL+25,y+5,innerL+65,y+45,0xfff8fcff,0xff8c9b94,Shader.TileMode.CLAMP));
                c.drawCircle(innerL+45,y+25,23,p);p.setShader(null);
                fill(0xff617965);c.drawCircle(innerL+45,y+25,7,p);fill(0xffdae9d8);c.drawCircle(innerL+45,y+25,3,p);
                text(c,fit("Play "+EmulatorLibrary.cleanTitle(game.name),innerR-innerL-115,29),innerL+89,y+35,29,-14930912,false,true);
            }else text(c,"Open Tray",innerL+89,y+35,29,-14930912,false,true);
        }

        void move(int d) {
            if (this.libraryMode) {
                int count = this.visibleGames().size();
                if (count > 0) {
                    this.gameIndex = Math.max(0, Math.min(count - 1, this.gameIndex + d));
                    this.clampGames(count);
                    this.sound(this.navSound);
                    this.invalidate();
                }
                return;
            }
            int total=this.rows[this.blade].length+(this.blade==3?0:1);
            this.row = this.row < 0 ? 0 : (this.row + d + total) % total;
            this.sound(this.navSound);
            this.invalidate();
        }

        void back() {
            if(tools!=null&&tools.back())return;
            if(media!=null && media.back())return;
            if(optionMode>=10){sound(backSound);optionMode=0;stopStick();invalidate();return;}
            if(optionMode!=0){sound(backSound);if(optionMode==1){optionMode=0;}else{optionMode=1;optionIndex=optionOffset=0;}stopStick();invalidate();return;}
            this.sound(this.backSound);
            if (this.libraryMode && !this.dialog) {
                this.libraryMode = false;
                this.row = 1;
                this.stopStick();
                this.invalidate();
                return;
            }
            if (this.dialog) {
                this.dialog = false;
            } else if (this.row >= 0) {
                this.row = -1;
            } else {
                this.dialog = true;
                this.title = "Blades 360";
                this.message = "Swipe left or right to change blades. Tap a menu item twice to open it.";
            }
            this.invalidate();
        }

        void activate() {
            if(tools!=null&&tools.active){tools.select();return;}
            if(media!=null && media.active){media.select();return;}
            if(optionMode!=0){activateOption();return;}
            if (this.libraryMode && !this.dialog) {
                this.libraryPlay();
                return;
            }
            if (this.dialog) {
                this.sound(this.backSound);
                this.dialog = false;
                this.invalidate();
                return;
            }
            if (this.row < 0) {
                this.sound(this.navSound);
                this.row = 0;
                this.invalidate();
                return;
            }
            this.sound(this.selectSound);
            if(this.blade==0&&this.row==2){openMarketplace();return;}
            if(this.blade==1&&this.row==0){openAchievements();return;}
            if(this.blade!=3 && this.row==this.rows[this.blade].length){playTray();return;}
            if (this.blade == 1) {
                if (this.row == 1) {
                    MainActivity.this.library.open();
                    return;
                }
                if (this.row == 2) {
                    MainActivity.this.library.emulators();
                    return;
                }
            }
            try {
                Intent intent = null;
                if (this.blade == 3) {
                    openSystem(10+this.row);return;
                } else if (this.blade == 2) {
                    if(this.row==3)openJellyfin();else media.open(this.row);return;
                }
                if (intent != null) {
                    MainActivity.this.startActivity(intent);
                    return;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.title = this.rows[this.blade][this.row];
            this.message = this.blade == 0 ? "Xbox Live services are not connected in this recreation." : "This feature is not connected to an Xbox console.";
            this.dialog = true;
            this.invalidate();
        }

        public boolean onTouchEvent(MotionEvent e) {
            float x = (e.getX() - this.offsetX) / this.scale;
            float y = e.getY() / this.scale;
            if(tools!=null&&tools.touch(e,x,y))return true;
            if(media!=null&&media.overlayTouch(e,x,y))return true;
            if(libraryMode && optionMode==0 && y>H-140 && y<H-95){if(e.getAction()==MotionEvent.ACTION_UP)tools.openHome();return true;}
            if(media!=null && media.active)return media.touch(e,x,y);
            if(media!=null && media.recording() && e.getAction()==MotionEvent.ACTION_UP && y<60 && x>W-280){media.stopRecording();return true;}
            if (e.getAction() == 0) {
                this.downX = x;
                this.downY = y;
                return true;
            }
            if (e.getAction() == 1) {
                int chosen;
                float dx = x - this.downX;
                if(optionMode!=0){float dy=y-downY;if(Math.abs(dy)>40){moveOption(dy<0?optionSlots:-optionSlots);return true;}return optionsTouch(x,y);}
                if (this.libraryMode) {
                    float dy = y - this.downY;
                    if (Math.abs(dy) > 40.0f) {
                        this.move(dy < 0.0f ? this.gameSlots : -this.gameSlots);
                        return true;
                    }
                    if (Math.abs(dx) > 80.0f) {
                        this.filterGames(dx < 0.0f ? 1 : -1);
                        return true;
                    }
                    return this.libraryTouch(x, y);
                }
                if (Math.abs(dx) > 80.0f) {
                    this.navigate(dx < 0.0f ? 1 : -1);
                    return true;
                }
                if (this.dialog) {
                    this.activate();
                    return true;
                }
                if (y > this.H - 96.0f) {
                    if (x > this.W / 2.0f) {
                        this.activate();
                    } else {
                        this.back();
                    }
                    return true;
                }
                if(this.blade!=3 && x>=this.left+49 && x<=this.right-27 && y>=this.H-185 && y<=this.H-114){this.row=this.rows[this.blade].length;playTray();return true;}
                if (x < this.left + 10.0f) {
                    this.navigate(-1);
                    return true;
                }
                if (x > this.right - 10.0f) {
                    this.navigate(1);
                    return true;
                }
                if (x >= this.menuX && x <= this.menuRight && y >= this.rowY && (chosen = (int)((y - this.rowY) / this.rowStep)) >= 0 && chosen < this.rows[this.blade].length) {
                    if (chosen == this.row) {
                        this.activate();
                    } else {
                        this.row = chosen;
                        this.sound(this.navSound);
                        this.invalidate();
                    }
                }
                return true;
            }
            return true;
        }

        void stopStick() {
            this.stickY = 0;
            this.stickX = 0;
            this.removeCallbacks(this.stickRepeat);
        }

        void controllerStep(int x, int y) {
            if(tools!=null&&tools.active){if(y!=0)tools.move(y);return;}
            if(media!=null&&media.overlayMove(x,y))return;
            if(media!=null && media.active){media.move(x,y);return;}
            if(optionMode!=0){if(y!=0)moveOption(y);return;}
            if (this.dialog) {
                return;
            }
            if (x != 0) {
                this.navigate(x);
            } else if (y != 0) {
                this.move(y);
            }
        }

        float controllerAxis(MotionEvent event, int axis) {
            InputDevice device = event.getDevice();
            InputDevice.MotionRange range = device == null ? null : device.getMotionRange(axis, event.getSource());
            float value = event.getAxisValue(axis);
            float dead = Math.max(library.prefs.getInt("deadzone",35)/100f, range == null ? 0.0f : range.getFlat());
            return Math.abs(value) > dead ? value : 0.0f;
        }

        public boolean onGenericMotionEvent(MotionEvent event) {
            if ((event.getSource() & 0x10) != 0 && event.getAction() == 2) {
                float x = this.controllerAxis(event, 15);
                float y = this.controllerAxis(event, 16);
                if (x == 0.0f && y == 0.0f) {
                    x = this.controllerAxis(event, 0);
                    y = this.controllerAxis(event, 1);
                }
                int nx = 0;
                int ny = 0;
                if (Math.abs(x) > Math.abs(y)) {
                    nx = x > 0.0f ? 1 : -1;
                } else if (y != 0.0f) {
                    int n = ny = y > 0.0f ? 1 : -1;
                }
                if (nx != this.stickX || ny != this.stickY) {
                    this.stopStick();
                    this.stickX = nx;
                    this.stickY = ny;
                    if (nx != 0 || ny != 0) {
                        this.controllerStep(nx, ny);
                        this.postDelayed(this.stickRepeat, 400L);
                    }
                }
                return true;
            }
            return super.onGenericMotionEvent(event);
        }

        boolean controllerKey(int key) {
            return key == 99 || key == 100 || key == 21 || key == 22 || key == 19 || key == 20 || key == 23 || key == 66 || key == 96 || key == 97 || key == 102 || key == 103;
        }

        public boolean onKeyUp(int key, KeyEvent event) {
            return this.controllerKey(key) || super.onKeyUp(key, event);
        }

        public boolean onKeyDown(int key, KeyEvent e) {
            if(media!=null&&(key==109||key==82||key==106||key==107||key==120)){if((key==109||key==82)&&tools!=null)tools.close();if(media.key(key,e))return true;}
            if(tools!=null&&tools.key(key,e))return true;
            if(media!=null && media.key(key,e))return true;
            if (!this.controllerKey(key)) {
                return super.onKeyDown(key, e);
            }
            if (e.getRepeatCount() > 0 && (key == 99 || key == 100 || key == 96 || key == 97 || key == 23 || key == 66)) {
                return true;
            }
            switch (key) {
                case 99: {
                    if (!this.libraryMode || optionMode!=0) break;
                    this.libraryActions();
                    break;
                }
                case 100: {
                    if (!this.libraryMode || optionMode!=0) break;
                    MainActivity.this.library.importGames();
                    break;
                }
                case 21: 
                case 102: {
                    this.controllerStep(-1, 0);
                    break;
                }
                case 22: 
                case 103: {
                    this.controllerStep(1, 0);
                    break;
                }
                case 19: {
                    this.controllerStep(0, -1);
                    break;
                }
                case 20: {
                    this.controllerStep(0, 1);
                    break;
                }
                case 23: 
                case 66: 
                case 96: {
                    this.activate();
                    break;
                }
                case 97: {
                    this.back();
                }
            }
            return true;
        }

        public void onWindowFocusChanged(boolean focused) {
            super.onWindowFocusChanged(focused);
            if (!focused) {
                this.stopStick();
            }
        }
    }
}
