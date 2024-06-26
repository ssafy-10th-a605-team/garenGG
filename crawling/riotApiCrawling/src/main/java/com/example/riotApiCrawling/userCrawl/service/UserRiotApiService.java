package com.example.riotApiCrawling.userCrawl.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.example.riotApiCrawling.userCrawl.dto.PlayerInfoDto;
import com.example.riotApiCrawling.userCrawl.dto.RequestDto;
import com.example.riotApiCrawling.userCrawl.entity.PlayerInfo;

import com.example.riotApiCrawling.userCrawl.repository.UserRiotApiRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserRiotApiService {

    private final UserRiotApiRepository userRiotApiRepository;
    @Value("${riot.apiKeys}")
    List<String> apiKeys;
    Map<String, Long> apiKeysId;
    public String[] rank = {"", "I", "II", "III", "IV"};

    UserRiotApiService(UserRiotApiRepository userRiotApiRepository){
        this.userRiotApiRepository = userRiotApiRepository;
    }
//    public void test(){
//        System.out.println("test시작.");
//        List<PlayerInfo> list = new ArrayList<>();
//        list = userRiotApiRepository.findByTierAndRank("PLATINUM","IV");
//        list = userRiotApiRepository.findByTier("PLATINUM");
//        System.out.println("한번에 가져올 수 있는 사이즈 : "+list.size()+"개");
//    }
    @PostConstruct
    public  void init() {
        apiKeysId = new HashMap<String, Long>();
        for(int i= 0; i < 13; i++){
            apiKeysId.put(apiKeys.get(i), (long)i);
        }
//        test();
    }

    public void crawlUser(RequestDto requestDto) throws IOException, InterruptedException {

        String tier = requestDto.getTier();
        int startRank = requestDto.getStartRank();
        int endRank = requestDto.getEndRank();
        int startPageNum = requestDto.getStartPageNum();
        int endPageNum = requestDto.getEndPageNum();
        String apiKey = requestDto.getApiKey();
        for(int  i = startRank;  i <= endRank; i++) {
            long startTime = System.currentTimeMillis(); // 시작 시간 측정
            System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")) +": "+tier + rank[i] + ", apikey :"+apiKeysId.get(apiKey)+ "  crawl start.");
            //티어 하나의 유저 목록 불러오기
            crawlUsersByTier(tier, rank[i], startPageNum, endPageNum, apiKey);
            long endTime = System.currentTimeMillis(); // 종료 시간 측정
            long totalTime = endTime - startTime;
            System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")) +": "+tier + rank[i] + ", apikey : " + apiKeysId.get(apiKey) + "Crwaling Done! execution time: " + totalTime + "ms");
        }
    }

    public void crawlUsersByTier(String tier, String rank, int startPageNum, int endPageNum, String apiKey) throws InterruptedException {
       
        int responseCode = 0;
        int pageNum = startPageNum;
        while (pageNum <= endPageNum) {
            try {
                URL url = new URL("https://kr.api.riotgames.com/lol/league-exp/v4/entries/RANKED_SOLO_5x5/" + tier + "/" + rank + "?page=" + pageNum + "&api_key=" + apiKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 요청 메서드 설정 (GET 방식)
                connection.setRequestMethod("GET");

                // 응답 코드 확인. 입/출력스트림가져오면서 암시적으로 네트워크연결.
                responseCode = connection.getResponseCode();
//                    System.out.println("Response Code: " + responseCode);

                // 응답 데이터 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // 연결 닫기
                connection.disconnect();

//                    System.out.println(pageNum + ", response : " + response);

                //플레이어 정보 담기
                List<PlayerInfoDto> playerInfoList = parseJsonResponseToPlayerInfoList(response.toString());

//                System.out.println("playerInfoList.size : "+playerInfoList.size());

                //지금까지 불러온 유저 목록에 대한 puuid 저장
                crawlPUUID(playerInfoList, apiKey);

                //지금까지 불러온 유저목록에대한 tagList 저장.
                crawlTagLine(playerInfoList, apiKey);

                //db저장.
                List<PlayerInfo> entityList = convertDtoListToEntityList(playerInfoList, apiKey);

                for (PlayerInfo entity : entityList) {
                    if(entity.getTagLine() == null || entity.getSummonerName() == null) continue;

                    PlayerInfo existingEntity = (PlayerInfo) userRiotApiRepository.findBySummonerNameAndTagLine(entity.getSummonerName(), entity.getTagLine()).orElse(null);

                    if (existingEntity != null) {
                        // 이미 존재하는 경우, ID를 설정하여 업데이트를 활성화
                        entity.setPlayerId(existingEntity.getPlayerId());
                    }
                }

                userRiotApiRepository.saveAll(entityList);

//                System.out.println("저장할 플레이어 목록");
//                for(PlayerInfoDto playerInfoDto : playerInfoList){
//                    System.out.println(playerInfoDto);
//                }

                if (playerInfoList.size() < 205) break; //한번에 205개씩 가져온다.

                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")) +": "+tier + rank + ", page "+pageNum+", apikey :"+apiKeysId.get(apiKey)+ "  crawl done.");

                pageNum++;
            } catch (IOException e) {
//                System.out.println(apiKeysId.get(apiKey)+" key, users per tier IOException. 10secs sleep. current Page : " + pageNum);
                Thread.sleep(10000);
            }
        }

    }

    //현재 playerInfoList에있는 정보에 PUUID 삽입
    private void crawlPUUID(List<PlayerInfoDto> playerInfoList, String apiKey) throws InterruptedException{
        int responseCode = 0;
        for(int count = 0 ; count < playerInfoList.size(); count++) {
            try {
                PlayerInfoDto playerInfoDto = playerInfoList.get(count);
                URL url = new URL("https://kr.api.riotgames.com/lol/summoner/v4/summoners/" + playerInfoDto.getSummonerId() + "?api_key=" + apiKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 요청 메서드 설정 (GET 방식)
                connection.setRequestMethod("GET");

                // 응답 코드 확인. 입/출력스트림가져오면서 암시적으로 네트워크연결.
                responseCode = connection.getResponseCode();
//                System.out.println("Response Code: " + responseCode);

                // 응답 데이터 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // 연결 닫기
                connection.disconnect();

                SummonerDto summonerDto = parseJsonResponseToSummonerDto(response.toString());

                //puuid설정.
                playerInfoList.get(count).setPuuid(summonerDto.getPuuid());

//                System.out.println("puuid 설정완료. count : "+ count);

            } catch (IOException e) {
                if(responseCode == 429) {
//                    System.out.println("puuid insert occured. 10secs rest. count : " + count);
                    count--;
                    Thread.sleep(10000);
                }
                else{
                    System.out.println("puuid not found. continue." + count);
                }
            } catch (NullPointerException e){
                System.out.println("null pointer exception. no summonerId. continue. count  : "+ count);
            } catch(Exception e){
                System.out.println("unexpected error during finding uuid. continue. :" );
            }
        }
    }

    private void crawlTagLine(List<PlayerInfoDto> playerInfoList, String apiKey) throws InterruptedException{
        int responseCode = 0;
        for(int count = 0 ; count < playerInfoList.size(); count++) {
            try {
                PlayerInfoDto playerInfoDto = playerInfoList.get(count);
                URL url = new URL("https://asia.api.riotgames.com/riot/account/v1/accounts/by-puuid/" + playerInfoDto.getPuuid() + "?api_key=" + apiKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 요청 메서드 설정 (GET 방식)
                connection.setRequestMethod("GET");

                // 응답 코드 확인. 입/출력스트림가져오면서 암시적으로 네트워크연결.
                responseCode = connection.getResponseCode();
//                System.out.println("Response Code: " + responseCode);

                // 응답 데이터 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // 연결 닫기
                connection.disconnect();

                AccountDto accountDto = parseJsonResponseToAccountDto(response.toString());

                //summoner name, tagline설정.
                playerInfoList.get(count).setTagLine(accountDto.getTagLine());
                playerInfoList.get(count).setSummonerName(accountDto.getGameName());

//                System.out.println("tagline setting done. count : "+ count + ", "+accountDto);

            } catch (IOException e) {
//                e.printStackTrace();
                if(responseCode == 429) {
//                    System.out.println("tagLine insert limit occured. 10secs rest. count : " + count);
                    count--;
                    Thread.sleep(10000);
                }
                else{
                    System.out.println("tagline not found. continue." + count);
                }
            }catch (NullPointerException e){
                System.out.println("null pointer exception. no PUUID. continue. count  : "+ count);
            } catch (Exception e){
                System.out.println("unexpected error during finding tagline. continue. :" );
            }

        }
    }


    private List<PlayerInfoDto> parseJsonResponseToPlayerInfoList(String jsonResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, new TypeToken<List<PlayerInfoDto>>() {
        }.getType());
    }

    private SummonerDto parseJsonResponseToSummonerDto(String jsonResponse){
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, new TypeToken<SummonerDto>(){}.getType());
    }

    private AccountDto parseJsonResponseToAccountDto(String jsonResponse){
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, new TypeToken<AccountDto>(){}.getType());
    }

    static class AccountDto{
        String puuid;
        String gameName;
        String tagLine;

        public String getPuuid() {
            return puuid;
        }

        public void setPuuid(String puuid) {
            this.puuid = puuid;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        public String getTagLine() {
            return tagLine;
        }

        public void setTagLine(String tagLine) {
            this.tagLine = tagLine;
        }
    }

    static class SummonerDto{
        String id;
        String accountId;
        String puuid;
        String name;
        String profileIconId;
        String reviseionDate;
        String summonerLevel;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getPuuid() {
            return puuid;
        }

        public void setPuuid(String puuid) {
            this.puuid = puuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfileIconId() {
            return profileIconId;
        }

        public void setProfileIconId(String profileIconId) {
            this.profileIconId = profileIconId;
        }

        public String getReviseionDate() {
            return reviseionDate;
        }

        public void setReviseionDate(String reviseionDate) {
            this.reviseionDate = reviseionDate;
        }

        public String getSummonerLevel() {
            return summonerLevel;
        }

        public void setSummonerLevel(String summonerLevel) {
            this.summonerLevel = summonerLevel;
        }
    }

    PlayerInfo convertDtoToEntity(PlayerInfoDto dto, String apiKey) {
        PlayerInfo entity = new PlayerInfo();
        entity.setPuuid(dto.getPuuid());
        entity.setTagLine(dto.getTagLine());
        entity.setLeagueId(dto.getLeagueId());
        entity.setQueueType(dto.getQueueType());
        entity.setTier(dto.getTier());
        entity.setRank(dto.getRank());
        entity.setSummonerId(dto.getSummonerId());
        entity.setSummonerName(dto.getSummonerName());
        entity.setLeaguePoints(dto.getLeaguePoints());
        entity.setWins(dto.getWins());
        entity.setLosses(dto.getLosses());
        entity.setVeteran(dto.isVeteran());
        entity.setInactive(dto.isInactive());
        entity.setFreshBlood(dto.isFreshBlood());
        entity.setHotStreak(dto.isHotStreak());
        entity.setApiKeyId(apiKeysId.get(apiKey));
        return entity;
    }
    private List<PlayerInfo> convertDtoListToEntityList(List<PlayerInfoDto> dtoList, String apiKey) {
        return dtoList.stream().map(dto -> convertDtoToEntity(dto, apiKey)).collect(Collectors.toList());
    }
}

