 package com.example.franciscojavier2.TalkTalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

 public class ActivitySettings extends AppCompatActivity {

    //Vistas del Layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusBtn;
    private Button mImageBtn;

    //Constante que declaro para el startActivityForResult
    private static final int GALLERY_PICK =1;

    //Firebase
     private StorageReference mImageStorage;
     private DatabaseReference mUserDatabase;
     private FirebaseUser mCurrentUser;
     private FirebaseAuth mAuth;

     //ProgressDialog
     ProgressDialog mProgressDialog;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Instancias del Layout
        mDisplayImage=(CircleImageView)findViewById(R.id.settings_image);
        mName=(TextView)findViewById(R.id.settings_name);
        mStatus=(TextView)findViewById(R.id.settings_status);
        mStatusBtn=(Button)findViewById(R.id.settings_status_btn);
        mImageBtn=(Button)findViewById(R.id.settings_image_btn);

        //Instancia del Storage Firebase
        mImageStorage= FirebaseStorage.getInstance().getReference();  //Esto apunta a la raíz del FirebaseStorage.

        //Instancia del User Firebase
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        //Instancia del Database Firebase
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
            //keepSynced sirve para que la instancia DatabaseReference esté siempre actualizada entre
            //lo que hay en la web o lo que hay en el smartphone en memoria. Para ello, hay que usarlo en
            //combinación con setPersistenceEnabled (Utilizado en la clase FirstClass) ya que éste lo que
            //permite es poder trabajar offline.

         mAuth = FirebaseAuth.getInstance();

        //Agrego un listener para establecer los valores en el layout
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default")){
                                    //Picasso es una librería importada.
                                    //Lo he metido en un if xq cuando creas un nuevo usuario no tiene una foto inicial y pone default
                                    //en Firebase. Entonces si no lo meto en un if, intenta establecer una imagen vacía cuando lo que
                                    //quiero es que deje la imagen por defecto que se ve en el xml.
                                    //placeholder pone una imagen mientras se carga la que realmente quiero poner.
                    Picasso.with(ActivitySettings.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {  //¿Qué pasa si la imagen
                                        //no está guardada y tenemos que descargarla online? Para eso añado un Callback
                                        //y pongo el código para descargar Online en onError().
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                            Picasso.with(ActivitySettings.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        //Botón de cambiar estado
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value=mStatus.getText().toString();   //Quiero enviar el status al ActivityStatus para mostrarlo.
                Intent status_intent=new Intent(ActivitySettings.this,ActivityStatus.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });

        //Botón de cambiar imagen
        mImageBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //PRIMERA FORMA DE SELECCIONAR UNA IMAGEN DEL DISPOSITIVO
                Intent galleryIntent=new Intent();  //Este Intent no es para abrir un nuevo activity sino para coger una
                                                    //imagen de la galería.
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

                //SEGUNDA FORMA DE SELECCIONAR UNA IMAGEN DEL DISPOSITIVO USANDO LA LIBRERÍA DE CROPPING
                //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(ActivitySettings.this);
            }
        });
    }

    //Override onActivityResult que será llamado desde el botón cambiar imagen
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){  //En este caso, no hubo errores en el startActivityForResult.
             Uri imageUri=data.getData();            //imageUri va a tener el Uri de la imagen que hayamos seleccionado previamente en el onClick.

                            // start cropping activity for pre-acquired image saved on the device
             CropImage.activity(imageUri).setAspectRatio(1,1).start(this);   //setAspectRatio pertenece a la librería importada Crop. Establece un ratio de la imágen 1:1 ya que me interesa que sea cuadrada.
                                                //El método start genera otro StartActivityForResult con el requestCode igual a CROP_IMAGE_ACTIVITY_REQUEST_CODE. Por eso, se ejecuta también el siguiente if.
         }
         if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode == RESULT_OK) {
                 //ProgressDialog
                 mProgressDialog=new ProgressDialog(ActivitySettings.this);
                 mProgressDialog.setTitle("Uploading image...");
                 mProgressDialog.setMessage("Please wait while uploading the image");
                 mProgressDialog.setCanceledOnTouchOutside(false);//para que el usuario no pueda cancelar el PRogressDialog
                 mProgressDialog.show();

                 Uri resultUri = result.getUri();   //result es un objeto de la clase CropImage.ActivityResult de la librería importada.
                 String current_user_id=mCurrentUser.getUid();

                 //Comprimo la imagen con una librería
                 File thumb_filePath=new File(resultUri.getPath());
                 Bitmap thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filePath);
                                //El argumento del setQaulity representa el % de calidad que quiero.
                 //Código copiado de Firebase para subir archivos al Storage de Firebase. Lo que hace es convertir un archivo
                 //en un OutputStream de bytes para poder subirlo a Firebase.
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                 final byte[] thumb_byte = baos.toByteArray();

                 final StorageReference thumb_filepath=mImageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");
                 final StorageReference filepath=mImageStorage.child("profile_images").child(current_user_id+".jpg");   //Le pongo a la imagen el nombre del id del usuario por ejemplo.
                                            //Si subo otra imagen para ese usuario se sobreescribirá la anterior con el mismo nombre.

                 filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {   //Pongo Listener aunque no es necesario para poner acciones
                                        //dependiendo de si la task ha sido succesful o no.
                     @Override
                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) { //los task tienen un comportamiento similar a los datasnapshot en cuanto a que contienen
                                            //la información del objeto que los llamó.
                         if(task.isSuccessful()) {
                             final String download_url = task.getResult().getDownloadUrl().toString();

                                //Línea copiada para subir el Array de bytes creado anteriormente para subir imágenes de Firebase.
                             UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                             uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                     String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                     if (thumb_task.isSuccessful()) {
                                         Map update_hashMap = new HashMap<>();
                                         update_hashMap.put("image", download_url);
                                         update_hashMap.put("thumb_image", thumb_downloadUrl);
                                         mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 if (task.isSuccessful()) {
                                                     mProgressDialog.dismiss();
                                                     Toast.makeText(ActivitySettings.this, "Success Uploaded", Toast.LENGTH_LONG).show();
                                                 }
                                             }
                                         });
                                     } else {
                                         Toast.makeText(ActivitySettings.this, "Error uploading thumbail", Toast.LENGTH_LONG).show();
                                         mProgressDialog.dismiss();
                                     }
                                 }
                             });
                         }
                     }
                 });
             } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                 Exception error = result.getError();
             }
         }
     }

     //Código para actualizar la clave online en la BD.
     @Override
     public void onStart() {
         super.onStart();
         mUserDatabase.child("online").setValue(true);
     }
     //Código para actualizar la clave online en la BD.
     protected void onPause() {
         super.onPause();
         mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
     }


 }
