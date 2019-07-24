package pubsub;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class Consumer implements Runnable {
    private String savePath;
    private int index;
    private Partition partition;
    private Trie trie;

    public Consumer(String savePath, Partition partition, int index) {
        this.savePath = savePath;
        this.partition = partition;
        this.index = index;
        this.trie = new Trie();
    }

    /*
        Consumer 요구사항
        2) 단어가 알파벳으로 시작한다면 단어의 첫 알파벳에 해당하는 파일 끝에 주어진 단어를 추가 해야한다.
　　　　　 예) apple, Apple은 a.txt 파일 끝에 추가 해야한다. (대소문자 구분없음)
　　　　3) 단어가 숫자로 시작한다면 number.txt 파일 끝에 주어진 단어를 추가 해야한다.
　　　　　 예) 1-point, 2-point는 number.txt 파일 끝에 추가 해야한다.
     */
    static public String makePath(String word) {
        char firstChar = word.charAt(0);
        String path = this.savePath + "\\";
        if (Character.isUpperCase(firstChar) == true) {
            String name = String.valueOf(firstChar);
            path += name.toLowerCase() + ".txt";
        } else if (Character.isLowerCase(firstChar) == true) {
            String name = String.valueOf(firstChar);
            path += name + ".txt";
        } else if (Character.isDigit(firstChar) == true) {
            path += "number.txt";
        }

        return path;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (this.partition.getWriteDone() && this.partition.count() == 0) {
                    System.out.println("partition[" + this.index + "] write done");
                    break;
                }
                /*
                    Consumer 요구사항
                    1) 파티션에서 순차적으로 단어를 1개씩 가져온다.
                 */
                String word = this.partition.pop();
                /*
                    Consumer 요구사항
                    4) 주어진 단어가 대상 파일에 이미 쓰여진 단어인지 대소문자 구분 없이 중복검사를 수행하고,
                 */
                String lowerCaseWord = word.toLowerCase();

                if (trie.search(lowerCaseWord)) {
                    System.out.println("ignore redundant word: " + word);
                    continue;
                } else {
                    trie.insert(lowerCaseWord);
                }
                String filePath = makePath(word);
                String finalWord = word + "\r\n";
                byte[] content = finalWord.getBytes();
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filePath, true);
                    try {
                        FileLock lock = out.getChannel().lock();
                        try {
                            out.write(content);
                        } catch(Exception e) {
                            System.out.println("Consumer[" + this.index + "] out.write Exception: " + e);
                        } finally {
                            if (lock != null) {
                                lock.release();
                            }
                        }
                    } catch(OverlappingFileLockException e) {
                        // 파일이 다른 thread에서 사용중이기 때문에, 롤백함
                        trie.delete(lowerCaseWord, 0, trie.root);
                        this.partition.push(word);
                        //System.out.println("-Consum0er[" + this.index + "] OverlappingFileLockException error: " + e);
                    } finally {
                        out.close();
                    }
                } catch(Exception e) {
                    System.out.println("Consumer[" + this.index + "] FileOutputStream Exception: " + e);
                }
                // 파일락 없는 버전
                /* try {
                    out = new FileOutputStream(filePath, true);
                    out.write(content);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    System.out.println("Consumer[" + this.index + "] Exception error: " + e);
                } */
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block e.printStackTrace();
        }
    }
}
