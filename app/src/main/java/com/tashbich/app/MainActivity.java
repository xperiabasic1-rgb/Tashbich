package com.tashbich.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.animation.ValueAnimator;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(11,16,27), PANEL=Color.rgb(25,33,52), GOLD=Color.rgb(224,190,125), MUTED=Color.rgb(157,166,184);
    private LinearLayout root, body, nav;
    private TextView countText, progressText, dhikrText, autoButton, soundLabel;
    private EditText targetInput;
    private BeadView beadView;
    private int mode=0, count=0, target=0, dhikr=0, interval=500, beadIndex=0;
    private boolean running=false, soundOn=true;
    private Handler handler=new Handler(Looper.getMainLooper());
    private Runnable autoRunner;
    private SoundPool pool;
    private int sTick,sChime,sFinal;
    private DB db;
    private final String[] names={"Subhanallah","Alhamdulillah","Allahu Akbar","Astaghfirullah","La ilaha illallah","Shalawat"};
    private final String[] arabic={"سُبْحَانَ اللَّهِ","الْحَمْدُ لِلَّهِ","اللَّهُ أَكْبَرُ","أَسْتَغْفِرُ اللَّهَ","لَا إِلَٰهَ إِلَّا اللَّهُ","اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ"};
    private final String[] bottom={"Bebas","Otomatis","Dzikir"};

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);db=new DB(this);load();pool=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();sTick=pool.load(this,R.raw.tick,1);sChime=pool.load(this,R.raw.chime,1);sFinal=pool.load(this,R.raw.chime_final,1);showCounter();}
    private void load(){SQLiteDatabase d=db.getReadableDatabase();android.database.Cursor c=d.rawQuery("SELECT c,t,m,dh,i FROM state WHERE id=1",null);if(c.moveToFirst()){count=c.getInt(0);target=c.getInt(1);mode=c.getInt(2);dhikr=c.getInt(3);interval=c.getInt(4);}c.close();}
    private void save(){db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO state(id,c,t,m,dh,i) VALUES(1,?,?,?,?,?)",new Object[]{count,target,mode,dhikr,interval});}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    private TextView txt(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(BG);b.setAllCaps(false);b.setTextSize(14);b.setTypeface(null,1);b.setBackground(shape(GOLD,70));return b;}
    private void base(String title,String subtitle){
        root=col();root.setBackgroundColor(BG);root.setPadding(22,14,22,10);setContentView(root);
        TextView brand=txt("T A S H B I C H",13,GOLD);brand.setGravity(Gravity.CENTER);brand.setTypeface(null,1);root.addView(brand,new LinearLayout.LayoutParams(-1,34));
        TextView h=txt(title,27,Color.WHITE);h.setTypeface(null,1);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=10;root.addView(h,hp);
        TextView sub=txt(subtitle,14,MUTED);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=5;root.addView(sub,sp);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout.LayoutParams scp=new LinearLayout.LayoutParams(-1,0,1);scp.topMargin=14;root.addView(scroll,scp);
        body=col();scroll.addView(body);
        nav=new LinearLayout(this);nav.setOrientation(0);nav.setGravity(Gravity.CENTER);nav.setPadding(5,6,5,6);nav.setBackground(shape(PANEL,35));LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,64);np.topMargin=10;root.addView(nav,np);
        for(int i=0;i<3;i++){final int x=i;TextView n=txt(bottom[i],14,mode==i?BG:MUTED);n.setGravity(Gravity.CENTER);n.setTypeface(null,1);if(mode==i)n.setBackground(shape(GOLD,50));nav.addView(n,new LinearLayout.LayoutParams(0,-1,1));n.setOnClickListener(v->{stopAuto();mode=x;save();showCounter();});}
    }
    private void showCounter(){
        String title=mode==0?"Tasbih Bebas":mode==1?"Tasbih Otomatis":"Tasbih Dzikir";
        String subtitle=mode==0?"Hitung tanpa dzikir tertulis":mode==1?"Manik bergerak dan terhitung otomatis":"Pilih dzikir, lalu hitung secara manual";
        base(title,subtitle);
        if(mode!=0){
            TextView label=txt("PILIH DZIKIR",11,MUTED);label.setTypeface(null,1);label.setLetterSpacing(.15f);body.addView(label);
            Spinner picker=new Spinner(this);picker.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));picker.setSelection(dhikr);picker.setBackground(shape(PANEL,30));body.addView(picker,new LinearLayout.LayoutParams(-1,48));
            picker.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?>p){}public void onItemSelected(android.widget.AdapterView<?>p,View v,int pos,long id){dhikr=pos;save();if(dhikrText!=null)dhikrText.setText(arabic[dhikr]);}});
        }
        LinearLayout card=col();card.setGravity(Gravity.CENTER);card.setPadding(16,18,16,18);card.setBackground(shape(PANEL,34));LinearLayout.LayoutParams cardp=new LinearLayout.LayoutParams(-1,0,1);cardp.topMargin=14;cardp.bottomMargin=14;body.addView(card,cardp);
        dhikrText=txt(mode==0?"بِسْمِ اللَّهِ":arabic[dhikr],27,GOLD);dhikrText.setGravity(Gravity.CENTER);dhikrText.setTypeface(null,1);card.addView(dhikrText);
        countText=txt("",58,Color.WHITE);countText.setGravity(Gravity.CENTER);countText.setTypeface(null,1);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=24;card.addView(countText,cp);
        progressText=txt("",15,MUTED);progressText.setGravity(Gravity.CENTER);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=4;card.addView(progressText,pp);
        beadView=new BeadView(this);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,160);bp.topMargin=10;bp.bottomMargin=8;card.addView(beadView,bp);
        beadView.setOnClickListener(v->{if(mode!=1)increment();});
        if(mode==1){autoButton=btn(running?"Jeda otomatis":"Mulai otomatis");card.addView(autoButton);autoButton.setOnClickListener(v->{if(running)stopAuto();else startAuto();autoButton.setText(running?"Jeda otomatis":"Mulai otomatis");});}
        else {Button tap=btn("KETUK UNTUK MENGHITUNG");card.addView(tap);tap.setOnClickListener(v->increment());}
        TextView targetLabel=txt(mode==0?"TARGET (OPSIONAL)":mode==1?"TARGET TASBIH":"TARGET DZIKIR",11,MUTED);targetLabel.setTypeface(null,1);targetLabel.setLetterSpacing(.14f);body.addView(targetLabel);
        LinearLayout targetRow=new LinearLayout(this);targetRow.setGravity(Gravity.CENTER_VERTICAL);LinearLayout.LayoutParams trp=new LinearLayout.LayoutParams(-1,54);trp.topMargin=8;body.addView(targetRow,trp);
        targetInput=new EditText(this);targetInput.setSingleLine(true);targetInput.setText(target==0?"":String.valueOf(target));targetInput.setHint("Kosong = tanpa batas");targetInput.setHintTextColor(MUTED);targetInput.setTextColor(Color.WHITE);targetInput.setInputType(2);targetInput.setPadding(15,0,10,0);targetInput.setBackground(shape(PANEL,24));targetRow.addView(targetInput,new LinearLayout.LayoutParams(0,-1,1));
        Button apply=btn("Terapkan");LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(110,-1);ap.leftMargin=8;targetRow.addView(apply,ap);apply.setOnClickListener(v->{try{String s=targetInput.getText().toString().trim();target=s.isEmpty()?0:Math.max(1,Integer.parseInt(s));save();update();}catch(Exception e){targetInput.setError("Target tidak valid");}});
        if(mode==1){
            TextView sl=txt("JARAK PER MANIK",11,MUTED);sl.setTypeface(null,1);sl.setLetterSpacing(.14f);LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2);slp.topMargin=15;body.addView(sl,slp);
            LinearLayout speeds=new LinearLayout(this);LinearLayout.LayoutParams ssp=new LinearLayout.LayoutParams(-1,46);ssp.topMargin=6;body.addView(speeds,ssp);
            Button half=btn("0,5 detik"),one=btn("1 detik");speeds.addView(half,new LinearLayout.LayoutParams(0,-1,1));LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(0,-1,1);op.leftMargin=8;speeds.addView(one,op);
            half.setOnClickListener(v->{interval=500;save();});one.setOnClickListener(v->{interval=1000;save();});
        }
        TextView sound=txt("Suara manik: "+(soundOn?"aktif":"nonaktif"),13,MUTED);LinearLayout.LayoutParams sndp=new LinearLayout.LayoutParams(-1,40);sndp.topMargin=5;body.addView(sound,sndp);sound.setGravity(Gravity.CENTER);sound.setOnClickListener(v->{soundOn=!soundOn;sound.setText("Suara manik: "+(soundOn?"aktif":"nonaktif"));});
        Button reset=btn("Reset hitungan");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,42);rp.topMargin=12;rp.bottomMargin=10;body.addView(reset,rp);reset.setOnClickListener(v->{stopAuto();count=0;beadIndex=0;save();update();beadView.invalidate();if(autoButton!=null)autoButton.setText("Mulai otomatis");});
        update();
    }
    private void update(){if(countText!=null){countText.setText(String.valueOf(count));progressText.setText(target>0?count+" / "+target:count+" hitungan");if(beadView!=null)beadView.invalidate();}}
    private void increment(){count++;beadIndex=(beadIndex+1)%33;save();update();if(beadView!=null)beadView.pulse();if(soundOn&&pool!=null){pool.play(sTick,.8f,.8f,1,0,1f);if(count%99==0)pool.play(sFinal,1f,1f,2,0,1f);else if(count%33==0)pool.play(sChime,1f,1f,2,0,1f);}if(target>0&&count>=target&&mode==1){stopAuto();if(autoButton!=null)autoButton.setText("Mulai otomatis");}}
    private void startAuto(){running=true;autoRunner=new Runnable(){public void run(){if(!running)return;increment();if(running)handler.postDelayed(this,interval);}};handler.postDelayed(autoRunner,interval);}
    private void stopAuto(){running=false;if(autoRunner!=null)handler.removeCallbacks(autoRunner);}
    @Override protected void onPause(){super.onPause();save();}
    @Override protected void onDestroy(){stopAuto();if(pool!=null)pool.release();super.onDestroy();}
    class BeadView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);float shift=0;ValueAnimator anim;
        final int[][] pal={{190,170,255},{255,165,190},{255,205,110}};
        BeadView(Context c){super(c);}
        void pulse(){if(anim!=null)anim.cancel();anim=ValueAnimator.ofFloat(1f,0f);anim.setDuration(220);anim.addUpdateListener(a->{shift=(float)a.getAnimatedValue();invalidate();});anim.start();}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2,sp=w/13f,top=h*.12f,sag=h*.5f;
            for(int k=-8;k<=8;k++){
                float dx=(k+shift)*sp;float n=Math.min(1f,Math.abs(dx)/(w/2f));if(n>=1f)continue;
                float x=cx+dx,y=top+sag*(1f-n*n);float r=7f+(1f-n)*5f;
                int id=count+k;boolean on=id>0&&k<=0;
                int[] q=pal[(Math.max(id-1,0)/33)%3];int col=on?Color.rgb(q[0],q[1],q[2]):Color.rgb(235,238,245);
                if(k==0&&shift<.5f){p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(60,q[0],q[1],q[2]));c.drawCircle(x,y,r+9,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(Color.argb(110,255,255,255));c.drawCircle(x,y,r+9,p);r+=3;}
                p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(x-r*.35f,y-r*.4f,r*1.4f,new int[]{Color.WHITE,col,Color.argb(255,Color.red(col)*2/3,Color.green(col)*2/3,Color.blue(col)*2/3)},new float[]{0f,.5f,1f},Shader.TileMode.CLAMP));
                c.drawCircle(x,y,r,p);p.setShader(null);
            }
        }
    }
    class DB extends SQLiteOpenHelper{DB(Context c){super(c,"tashbich.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE state(id INTEGER PRIMARY KEY,c INTEGER,t INTEGER,m INTEGER,dh INTEGER,i INTEGER)");d.execSQL("INSERT INTO state VALUES(1,0,0,0,0,500)");}public void onUpgrade(SQLiteDatabase d,int o,int n){}}
}
