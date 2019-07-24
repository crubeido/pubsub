package pubsub;

import java.time.Duration;
import java.time.Instant;
public class App 
{
    public static void main( String[] args ) {
        String filePath = null;
        String savePath = null;
        int numOfPartition = 0;
        /*
            요구사항
            1. Maven 기반으로 프로젝트 구성
            2. 프로그램의 실행 argument로 3개의 값을 입력
        　　　- 처리해야 할 입력 파일명
        　　　- 결과 파일들을 저장 할 디렉토리 경로
        　　　- 병렬 처리를 위한 파티션 수 N (1 < N < 28)
         */
        for (int i = 0; i < args.length; i++) {
            if (0 == i) {
                filePath = args[i];
                System.out.println("filePath: " + filePath);
            } else if (1 == i) {
                savePath = args[i];
                System.out.println("savePath: " + savePath);
            } else if (2 == i) {
                numOfPartition = Integer.parseInt(args[i]);
                if (1 < numOfPartition && numOfPartition < 28) {
                    System.out.println("numOfPartition: " + numOfPartition);
                } else {
                    System.out.println("numOfPartition range failed");
                    return;
                }
            }
        }

        // 처리시간 게산을 위한 시작시간 구하기
        Instant start = Instant.now();
        {
            /*
                요구사항
                3. Producer-Consumer 디자인 패턴을 응용해 아래의 요구사항에 따라 로직 구현
             */
            Partition partitions[] = new Partition[numOfPartition];
            for (int index = 0; index < numOfPartition; index++) {
                partitions[index] = new Partition(index);
            }

            Thread producer = new Thread(new Producer(filePath, partitions));
            producer.start();

            Thread consumers[] = new Thread[numOfPartition];
            for (int index = 0; index < numOfPartition; index++) {
                consumers[index] = new Thread(new Consumer(savePath, partitions[index], index));
                consumers[index].start();
            }

        /*
            요구사항
            4. 프로그램 종료시 사용된 리소스를 올바르게 정리
         */
            for (int j = 0; j < numOfPartition; j++) {
                consumers[j].join(); //todo add catch exception
            }
            producer.join();

        }
        // 종료시간 구한뒤 시작시간과 시간 계산
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
    }
}
