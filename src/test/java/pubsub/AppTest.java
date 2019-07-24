package pubsub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test.
 */
public class AppTest
{
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
}
