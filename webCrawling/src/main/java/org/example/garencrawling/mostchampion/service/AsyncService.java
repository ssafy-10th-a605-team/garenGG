package org.example.garencrawling.mostchampion.service;

import lombok.RequiredArgsConstructor;
import org.example.garencrawling.global.GlobalConstants;
import org.example.garencrawling.mostchampion.domain.MostData;
import org.example.garencrawling.mostchampion.domain.PlayerCurSoloRank;
import org.example.garencrawling.mostchampion.domain.PlayerInfo;
import org.example.garencrawling.mostchampion.repository.PlayerCurSoloRankRepository;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@EnableMongoRepositories(basePackageClasses = {PlayerCurSoloRankRepository.class})
public class AsyncService {

    private final PlayerCurSoloRankRepository playerCurSoloRankRepository;

    @Async("threadPoolTaskExecutor")
    public void processPlayersInRange(List<PlayerInfo> subFindedPlayerInfos, int threadNumber) throws InterruptedException {

        int currentStartIndex = 0;
        int endIndex = subFindedPlayerInfos.size() - 1;

        while (currentStartIndex <= endIndex) {

            int currentEndIndex = Math.min(currentStartIndex + GlobalConstants.saveSize - 1, endIndex);

            System.out.println("현재 시간: " + GlobalConstants.formatter.format(new Date()) + " threadNumber = " + threadNumber + " " + currentStartIndex * 100 / endIndex + "%");

            crawling(subFindedPlayerInfos.subList(currentStartIndex, currentEndIndex + 1), threadNumber);
            currentStartIndex = currentEndIndex + 1;
        }

        System.out.println("현재 시간: " + GlobalConstants.formatter.format(new Date()) + " threadNumber = " + threadNumber + " 종료");
    }

    public void crawling(List<PlayerInfo> playerInfos, int threadNumber) throws InterruptedException {

        ChromeDriver driver;
        WebDriverWait wait;

        WebElement element;
        List<WebElement> rows;

        ArrayList<PlayerCurSoloRank> playerCurSoloRanks = new ArrayList<>();
        int tryCount;
        String getText = null;

        ////////////////////////////////////////////////////////////////////////////////////////////

        driver = new ChromeDriver(GlobalConstants.options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(GlobalConstants.waitTime)); // 명시적 대기 시간 설정

        ////////////////////////////////////////////////////////////////////////////////////////////

        try {
            for (int index = 0; index < playerInfos.size(); index++) {
                StringBuilder sb = new StringBuilder();
                sb.append("threadNumber = ").append(threadNumber).append(" ");

                PlayerInfo playerInfo = playerInfos.get(index);
                sb.append("playerInfo.getPlayerId() = ").append(playerInfo.getPlayerId()).append(" ");
                sb.append("playerInfo.getUserNickname() = ").append(playerInfo.getUserNickname()).append(" ");

                PlayerCurSoloRank playerCurSoloRank = new PlayerCurSoloRank();
                playerCurSoloRank.setPlayerId(playerInfo.getPlayerId());

                // 메인 접속
                driver.get("https://fow.kr/find/" + playerInfo.getUserNickname());

                // 닉네임
                tryCount = 1;
                while (tryCount <= GlobalConstants.tryMaxCount) {
                    try {
                        getText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content-container > div:nth-child(1) > div:nth-child(2) > div.topp > div.profile > div:nth-child(2) > span.username"))).getText();
                        if (!getText.isEmpty()) {
                            break;
                        }
                    } catch (Exception e) {

                    }
                    driver.navigate().refresh();
                    tryCount++;
                }
                if (tryCount > GlobalConstants.tryMaxCount) {
                    sb.append("닉네임 tryCount 초과");
                    // System.out.println(sb);
                    continue;
                }

                // 갱신 가능 버튼
                try {
                    element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#content-container > div:nth-child(1) > div:nth-child(2) > div.topp > div.profile > div:nth-child(2) > div")));
                    driver.executeScript("arguments[0].click();", element);
                    Thread.sleep(5000);
                } catch (Exception e) {

                }

                // 현재 티어
                tryCount = 1;
                while (tryCount <= GlobalConstants.tryMaxCount) {
                    try {
                        getText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content-container > div:nth-child(1) > div:nth-child(2) > div.table_summary > div:nth-child(2) > div:nth-child(2) > b > font"))).getText();
                        if (!getText.isEmpty()) {
                            break;
                        }
                    } catch (Exception e) {

                    }
                    driver.navigate().refresh();
                    tryCount++;
                }
                if (tryCount > GlobalConstants.tryMaxCount) {
                    sb.append("현재 티어 tryCount 초과");
                    // System.out.println(sb);
                    continue;
                }


                try {
                    String tier = getText.split(" ")[0];
                    tier = tier.toLowerCase();
                    tier = Character.toUpperCase(tier.charAt(0)) + tier.substring(1);
                    playerCurSoloRank.setTier(tier);

                    if (!(tier.equals("Master") || tier.equals("Grandmaster") || tier.equals("Challenger"))) {
                        String rankNum = getText.split(" ")[1];
                        if (rankNum.equals("I")) {
                            playerCurSoloRank.setRankNum("1");
                        } else if (rankNum.equals("II")) {
                            playerCurSoloRank.setRankNum("2");
                        } else if (rankNum.equals("III")) {
                            playerCurSoloRank.setRankNum("3");
                        } else if (rankNum.equals("IV")) {
                            playerCurSoloRank.setRankNum("4");
                        }
                    }
                } catch (Exception e) {
                    sb.append("현재 티어 문자열 오류");
                    // System.out.println(sb);
                    continue;
                }

                // 솔로 랭크 버튼
                tryCount = 1;
                while (tryCount <= GlobalConstants.tryMaxCount) {
                    try {
                        element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#content-container > div:nth-child(1) > div:nth-child(2) > div.rankchamp_S14A_menu > a.sbtn.rankchamp_list.rankchamp_list_solo")));
                        driver.executeScript("arguments[0].click();", element);
                        break;
                    } catch (Exception e) {

                    }
                    driver.navigate().refresh();
                    tryCount++;
                }
                if (tryCount > GlobalConstants.tryMaxCount) {
                    sb.append("솔로 랭크 버튼 tryCount 초과");
                    // System.out.println(sb);
                    continue;
                }

                // 챔피언 목록
                tryCount = 1;
                while (tryCount <= GlobalConstants.tryMaxCount) {
                    try {
                        wait.until(new ExpectedCondition<Boolean>() {
                            public Boolean apply(WebDriver driver) {
                                List<WebElement> elements = driver.findElements(By.cssSelector("#content-container > div:nth-child(1) > div:nth-child(2) > div.rankchamp_S14A_div.rankchamp_S14A_div_solo > table > tbody > tr"));
                                for (WebElement element : elements) {
                                    if (!element.isDisplayed()) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        });
                        rows = driver.findElements(By.cssSelector("#content-container > div:nth-child(1) > div:nth-child(2) > div.rankchamp_S14A_div.rankchamp_S14A_div_solo > table > tbody > tr"));

                        int mostSeq = 1;
                        playerCurSoloRank.setMostDatas(new ArrayList<>());

                        for (WebElement row : rows) {
                            MostData mostData = new MostData();
                            mostData.setMostSeq(String.valueOf(mostSeq));
                            mostSeq++;

                            List<WebElement> cols = row.findElements(By.tagName("td"));
                            for (int i = 0; i < cols.size(); i++) {
                                String tmp = cols.get(i).getText();

                                if (i == 0) {
                                    mostData.setChampion(GlobalConstants.championNames.get(tmp));
                                } else if (i == 1) {
                                    mostData.setGame(tmp);
                                } else if (i == 2) {
                                    double percentage = Double.parseDouble(tmp.substring(0, tmp.length() - 1));
                                    int win = (int) (Integer.parseInt(mostData.getGame()) * percentage) / 100;
                                    int lose = Integer.parseInt(mostData.getGame()) - win;

                                    String result = "";

                                    if (win != 0)
                                        result = result + win + "W";
                                    if (lose != 0)
                                        result = result + lose + "L";

                                    result = result + (int) percentage + "%";

                                    mostData.setGame(result);
                                }
                            }
                            playerCurSoloRank.getMostDatas().add(mostData);
                        }
                        sb.append("----------------------성공");
                        // System.out.println(sb);
                        playerCurSoloRanks.add(playerCurSoloRank);
                        break;
                    } catch (Exception e) {

                    }
                    tryCount++;
                }
                if (tryCount > GlobalConstants.tryMaxCount) {
                    sb.append("챔피언 목록 tryCount 초과");
                    // System.out.println(sb);
                    continue;
                }
            }
        } catch (Exception e) {

        }

        driver.quit();
        playerCurSoloRankRepository.saveAll(playerCurSoloRanks);

    }
}