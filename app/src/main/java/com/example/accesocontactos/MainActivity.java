package com.example.accesocontactos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    RecyclerView rv;
    ArrayList<String> listaContactos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv=findViewById(R.id.rv);




        //Pedir permiso para mandar SMSs, si no lo tenemos
        int result= ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS);
        if(result== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
        }


        int result2= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        System.out.println("----->"+result2);
        if(result2== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},2);
        }

        //Cargo los contactos en la lista de contactos

        listaContactos=new ArrayList<String>();

        Uri uri= ContactsContract.Contacts.CONTENT_URI;
        String[] proyeccion={ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String seleccion=null;
        String[] args_seleccion=null;

        ContentResolver cr=getContentResolver();

        Cursor cursor=cr.query(uri,proyeccion,seleccion,args_seleccion,null);

        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                String id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String nombreContacto=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String tieneTlf=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));


                //Añado el contacto a la lista
                listaContactos.add(nombreContacto);
            }
        }
        cursor.close();




        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);

        rv.setLayoutManager(linearLayoutManager);

        AdaptadorPersonalizado adaptadorPersonalizado=new AdaptadorPersonalizado();

        rv.setAdapter(adaptadorPersonalizado);

    }

    class AdaptadorPersonalizado extends RecyclerView.Adapter<AdaptadorPersonalizadoHolder>{
        @NonNull
        @Override
        public AdaptadorPersonalizadoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflamos vista
            View viewAInflar=getLayoutInflater().inflate(R.layout.contactos,parent,false);

            return new AdaptadorPersonalizadoHolder(viewAInflar);
        }

        @Override
        public void onBindViewHolder(@NonNull AdaptadorPersonalizadoHolder holder, int position) {
            holder.fijarValores(position);
        }

        @Override
        public int getItemCount() {
            return listaContactos.size();
        }
    }

    private class AdaptadorPersonalizadoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv;

        public AdaptadorPersonalizadoHolder(@NonNull View itemView) {
            super(itemView);

            tv=itemView.findViewById(R.id.textView2);

            itemView.setOnClickListener((View.OnClickListener)this);
        }

        public void fijarValores(int position) {
            tv.setText(listaContactos.get(position));
        }

        public void onClick(View view){

            //Obtenemos el tlf a apartir del nombre de contacto
            //Primero obtenemos la id del contacto. Luego con la id obtenemos el tlf

            int indice=getLayoutPosition();
            String nombreContacto=listaContactos.get(indice);

            Uri uri= ContactsContract.Contacts.CONTENT_URI;
            String[] proyeccion={ContactsContract.Contacts._ID};
            String seleccion=ContactsContract.Contacts.DISPLAY_NAME+"='"+nombreContacto+"'";
            String[] args_seleccion=null;

            ContentResolver cr=getContentResolver();

            Cursor cursor=cr.query(uri,proyeccion,seleccion,args_seleccion,null);

            String id=null;
            if(cursor.getCount()>0){
                while(cursor.moveToNext()){
                    id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                }
            }
            cursor.close();

            String mensaje="Holaa";
            enviarSMS(id,mensaje);





        }
    }

    private void enviarSMS(String id, String mensajeAEnviar) {
        String mensaje=mensajeAEnviar;

        Uri uri= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] proyeccion={ContactsContract.CommonDataKinds.Phone.NUMBER};
        String seleccion=ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"=?";
        String[] args_seleccion=new String[]{id};

        ContentResolver cr=getContentResolver();

        Cursor cursor=cr.query(uri,proyeccion,seleccion,args_seleccion,null);

        String tlf=null;
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                tlf=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                SmsManager smsManager = SmsManager.getDefault();
                try {
                    smsManager.sendTextMessage(tlf, null, mensaje, null, null);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                }

            }
        }
        cursor.close();

    }


}