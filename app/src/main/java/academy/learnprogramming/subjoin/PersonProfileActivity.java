package academy.learnprogramming.subjoin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity
{
    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendReqbutton, DeclineFriendRequestbutton;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();

        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue(String.class);
                    String myUserName = dataSnapshot.child("username").getValue(String.class);
                    String myProfilName = dataSnapshot.child("fullname").getValue(String.class);
                    String myProfileStatus = dataSnapshot.child("status").getValue(String.class);
                    String myDOB = dataSnapshot.child("dob").getValue(String.class);
                    String myCountry = dataSnapshot.child("country").getValue(String.class);
                    String myGender = dataSnapshot.child("gender").getValue(String.class);
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue(String.class);


                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUserName) ;
                    userProfName.setText(myProfilName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB: " + myDOB);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relationship: " + myRelationStatus);

                    MaintananceofButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestbutton.setEnabled(false);

        if (!senderUserId.equals(receiverUserId))
        {
            SendFriendReqbutton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    SendFriendReqbutton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnfriendAnExistingFriend();
                    }
                }
            });

        }
        else
        {
            DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
            SendFriendReqbutton.setVisibility(View.INVISIBLE);

        }

    }


    private void UnfriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>(){
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                SendFriendReqbutton.setText("Send Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void AcceptFriendRequest()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>(){
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>(){
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful()) {
                                                                                        SendFriendReqbutton.setEnabled(true);
                                                                                        CURRENT_STATE = "Friends";
                                                                                        SendFriendReqbutton.setText("Unfriend this Person");

                                                                                        DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendRequestbutton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });
    }


    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>(){
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                SendFriendReqbutton.setText("Send Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void MaintananceofButtons()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendFriendReqbutton.setText("Cancel Friend request");

                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestbutton.setEnabled(false);
                            }
                            else if (request_type.equals("received"))
                            {


                                CURRENT_STATE = "request_received";
                                SendFriendReqbutton.setText("Accept friend Request");

                                DeclineFriendRequestbutton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestbutton.setEnabled(true);

                                DeclineFriendRequestbutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelFriendRequest();
                                    }
                                });

                            }
                        }
                        else
                        {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE = "friends";
                                                SendFriendReqbutton.setText("Unfriend this Person");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
    }


    private void SendFriendRequestToaPerson()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>(){
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            SendFriendReqbutton.setEnabled(true);
                                            CURRENT_STATE = "request_sent";
                                            SendFriendReqbutton.setText("Cancel friend Request");

                                            DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestbutton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }


    private void InitializeFields()
    {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView) findViewById(R.id.person_full_name);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelation = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        SendFriendReqbutton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestbutton = (Button) findViewById(R.id.person_decline_friend_request);

        CURRENT_STATE = "not_friends";

    }
}
