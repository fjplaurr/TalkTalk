package com.example.franciscojavier2.TalkTalk;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by FranciscoJavier2 on 08/12/2017.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter{

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                FragmentFriendshipReq requestsFragment=new FragmentFriendshipReq();
                return requestsFragment;
            case 1:
                FragmentConversation chatsFragment=new FragmentConversation();
                return chatsFragment;
            case 2:
                FragmentFriends friendsFragment=new FragmentFriends();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;           //Aquí hay que devolver el número de vistas. En mi caso, tengo 3 Fragments.
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }

}
