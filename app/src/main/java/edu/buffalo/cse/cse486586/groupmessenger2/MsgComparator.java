package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

/**
 * Created by shivamgupta on 3/22/17.
 */

public class MsgComparator implements Comparator<MessageParam>{

    @Override
    public int compare(MessageParam ob, MessageParam ob1) {

            if(ob.a_seq < ob1.a_seq){
                return -1;
            }

            if(ob.a_seq > ob1.a_seq) {
                return 1;
            }

            if(ob.a_seq == ob1.a_seq){
                if(Integer.parseInt(ob.myport) < Integer.parseInt(ob1.myport)){
                    return -1;
                }
                else{
                    return 1;
                }
            }

        return 0;
    }


}
