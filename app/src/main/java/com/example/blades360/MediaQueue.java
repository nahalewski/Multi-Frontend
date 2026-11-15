package com.example.blades360;

import java.util.*;

/** Chooses the next library index without mutating playlist membership. */
final class MediaQueue {
  static int next(List<Integer> queue,int current,int direction,boolean completed,int repeat,boolean shuffle,Random random){
    if(queue.isEmpty())return -1;
    if(completed&&repeat==2&&queue.contains(current))return current;
    if(shuffle&&queue.size()>1){ArrayList<Integer> choices=new ArrayList<>(queue);choices.remove(Integer.valueOf(current));return choices.get(random.nextInt(choices.size()));}
    int position=queue.indexOf(current)+(direction>0?1:-1);
    if(!queue.contains(current))position=0;
    if(position>=queue.size()){if(completed&&repeat==0)return -1;position=0;}
    if(position<0)position=queue.size()-1;
    return queue.get(position);
  }
  static boolean validName(String value){return value!=null&&!value.trim().isEmpty()&&value.length()<=90&&!value.equals(".")&&!value.equals("..")&&!value.matches(".*[/\\\\\\p{Cntrl}].*");}
}
