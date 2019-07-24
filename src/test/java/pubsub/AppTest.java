package pubsub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test.
 */
public class AppTest
{
    /*
        요구사항
        - 병렬 처리를 위한 파티션 수 N (1 < N < 28)
     */
    @Test
    public void partitionRangeTest() {
        int numOfPartition = 0;
        assertFalse(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 1;
        assertFalse(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 5;
        assertTrue(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 10;
        assertTrue(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 15;
        assertTrue(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 20;
        assertTrue(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 27;
        assertTrue(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 28;
        assertFalse(1 < numOfPartition && numOfPartition < 28);
        numOfPartition = 29;
        assertFalse(1 < numOfPartition && numOfPartition < 28);
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
    @Test
    public void validatePatternTest() {
        Partition partitions[] = new Partition[2];
        for (int index = 0; index < 2; index++) {
            partitions[index] = new Partition(index);
        }
        Producer producer = new Producer("c:", partitions);

        assertTrue(producer.validatePattern("ab!23"));
        assertTrue(producer.validatePattern("A12bd"));
        assertTrue(producer.validatePattern("123abc"));
        assertFalse(producer.validatePattern("#abc"));
        assertFalse(producer.validatePattern("-bddf"));
        assertFalse(producer.validatePattern("````1c"));
    }

    /*
       Producer 요구사항
       4) 유효한 단어들은 N개의 파티션으로 나눠서 Consumer에 전달한다.
          - 동일한 단어는 항상 동일한 파티션에 포함되어야 한다.
   */
    @Test
    public void getHashByFoldingTest() {
        Partition partitions[] = new Partition[2];
        for (int index = 0; index < 2; index++) {
            partitions[index] = new Partition(index);
        }
        Producer producer = new Producer("c:", partitions);

        int firstPartitionNum = 0;
        int secondPartitionNum = 1;

        // 동일한 문자는 동일한 파티션 number를 획득
        firstPartitionNum = producer.getHashByFolding("hello", 10);
        secondPartitionNum = producer.getHashByFolding("hello", 10);
        assertTrue(firstPartitionNum == secondPartitionNum);

        // 파티션수가 달라져도 동일한 문자에 대해서는 동일한 파티션 number를 획득
        firstPartitionNum = producer.getHashByFolding("hello", 20);
        secondPartitionNum = producer.getHashByFolding("hello", 20);
        assertTrue(firstPartitionNum == secondPartitionNum);

        // 대소문자 구분이 없기 때문에 동일한 문자로 간주하여 동일한 파티션 number 획득
        firstPartitionNum = producer.getHashByFolding("hello", 20);
        secondPartitionNum = producer.getHashByFolding("HELlo", 20);
        assertTrue(firstPartitionNum == secondPartitionNum);
    }

    /*
        Consumer 요구사항
        4) 주어진 단어가 대상 파일에 이미 쓰여진 단어인지 대소문자 구분 없이 중복검사를 수행하고,
     */
    @Test
    public void trieTest() {
        String keys[] = {"the", "there", "apple", "app", "abp", "dino"};
        Trie trie = new Trie();
        for (int i = 0; i < keys.length; i++) {
            trie.insert(keys[i]);
        }
        assertTrue(trie.search("the"));
        assertTrue(trie.search("there"));
        assertTrue(trie.search("apple"));
        assertTrue(trie.search("app"));
        assertTrue(trie.search("abp"));
        assertTrue(trie.search("dino"));
        assertFalse(trie.search("th"));
        assertFalse(trie.search("ther"));
        assertFalse(trie.search("ap"));
        assertFalse(trie.search("a"));
        assertFalse(trie.search("2dd"));

        trie.delete("the", 0, trie.root);
        assertFalse(trie.search("the"));

        trie.delete("apple", 0, trie.root);
        assertFalse(trie.search("apple"));

        trie.delete("there", 0, trie.root);
        assertFalse(trie.search("there"));
    }

    /*
        Consumer 요구사항
        2) 단어가 알파벳으로 시작한다면 단어의 첫 알파벳에 해당하는 파일 끝에 주어진 단어를 추가 해야한다.
　　　　　 예) apple, Apple은 a.txt 파일 끝에 추가 해야한다. (대소문자 구분없음)
　　　　3) 단어가 숫자로 시작한다면 number.txt 파일 끝에 주어진 단어를 추가 해야한다.
　　　　　 예) 1-point, 2-point는 number.txt 파일 끝에 추가 해야한다.
     */
    @Test
    public void makePathTest() {
        Partition partition = new Partition(0);
        Consumer consumer = new Consumer("c:", partition, 0);

        assertTrue(consumer.makePath("apple").matches("c:\\\\a.txt"));
        assertTrue(consumer.makePath("1-point").matches("c:\\\\number.txt"));
        assertTrue(consumer.makePath("2-point").matches("c:\\\\number.txt"));
        assertTrue(consumer.makePath("zip").matches("c:\\\\z.txt"));
        assertTrue(consumer.makePath("die").matches("c:\\\\d.txt"));
        assertTrue(consumer.makePath("2eeee").matches("c:\\\\number.txt"));
    }
}
