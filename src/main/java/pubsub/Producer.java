package pubsub;

import java.io.*;
import java.util.regex.Pattern;

public class Producer implements Runnable {
    private String filePath;
    private Partition partitions[];

    public Producer(String filePath, Partition partitions[]) {
        this.filePath = filePath;
        this.partitions = partitions;
    }

    /*
        Producer 요구사항
        2) 각 라인의 주어진 단어가 유효한지 정규표현식을 활용해 검사한다.
           - 유효한 단어에 대한 설명은 과제설명 항목2 참고
           - words.txt 파일의 각 라인은 1개의 단어를 포함하고 있습니다.
　　　        · 단어는 알파벳 또는 숫자로 시작하며 대소문자는 구분하지 않습니다.
            　· 알파벳이나 숫자가 아닌 문자로 시작하는 단어는 유효하지 않습니다.
        　　　　예) ab!23 (유효함), A12bd (유효함), 123abc (유효함), #abc (유효하지않음)
     */
    public boolean validatePattern(String word) {
        String firstChar = String.valueOf(word.charAt(0));
        String pattern = "^[a-zA-Z0-9]*$";
        boolean res = Pattern.matches(pattern, firstChar);
        // Return true only if line[0] is in [A-Za-z0-9].
        if (res) {
            return true;
        }
        return false;
    }

    /*
        Producer 요구사항
        4) 유효한 단어들은 N개의 파티션으로 나눠서 Consumer에 전달한다.
           - 동일한 단어는 항상 동일한 파티션에 포함되어야 한다.
    */
    public int getHashByFolding(String key, int numOfPartition) {
        // 대소문자 구분없이 동일한 파티션으로 보내기 위해 소문자로 강제변환
        String lowerKey = key.toLowerCase();
        int hashValue = 0;

        for (int i = 0; i < lowerKey.length(); i++)
            hashValue = (hashValue << 3) + hashValue + lowerKey.charAt(i);

        if (hashValue < 0)
            hashValue = -hashValue;

        return hashValue % numOfPartition;
    }

    @Override
    public void run() {
        try {
            File file = new File(this.filePath);
            FileReader filereader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(filereader);
            String word;
            /*
                Producer 요구사항
                1) 파일에서 각 라인을 읽어온다.
            */
            while ((word = bufReader.readLine()) != null) {
                /*
                    Producer 요구사항
                    3) 유효하지 않은 단어들은 처리를 생략한다.
                 */
                if (this.validatePattern(word)) {
                    /*
                        Producer 요구사항
                        4) 유효한 단어들은 N개의 파티션으로 나눠서 Consumer에 전달한다.
                     */
                    int hash = getHashByFolding(word, this.partitions.length);
                    this.partitions[hash].push(word);
                }/*else {
                    System.out.println(word);// + ": not valid");
                }*/
            }
            System.out.println("write done all");
            bufReader.close();

            for (int i = 0; i < this.partitions.length; i++) {
                this.partitions[i].setWriteDone(true);
            }

            // Check load balancing & data written to partitions
            /*FileWriter writer = null;
            try {
                for (int i = 0; i < this.partitions.length; i++) {
                    System.out.println("partition[" + i + "]: " + this.partitions[i].count());
                    String path = "partition" + i + ".txt";
                    File f = new File(path);

                    for (int j = 0; j < this.partitions[i].count(); j++) {
                        //System.out.println(this.partitions[i].word(j));
                        writer = new FileWriter(f, true);
                        writer.write(this.partitions[i].word(j) + "\r\n");
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Producer Exception: " + e);
        }
    }
}