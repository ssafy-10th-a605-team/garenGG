<template>
  <div>
    <Header />
    <section id="hero" class="hero">
      <img src="@/assets/garen-background.gif" alt="" data-aos="fade-in" />

      <div class="container">
        <div class="row">
          <div class="col-lg-10">
            <h2 data-aos="fade-up" data-aos-delay="100">GAREN.GG</h2>
            <p data-aos="fade-up" data-aos-delay="200">
              나에게 맞는 새로운 챔피언을 추천 받으세요
            </p>
          </div>
          <div class="col-lg-5">
            <div
              class="sign-up-form d-flex"
              data-aos="fade-up"
              data-aos-delay="300"
            >
              <input
                type="text"
                class="form-control"
                @keyup.enter="searchMyChamp"
                v-model="word"
                placeholder="소환사명#태그"
              />
              <button class="btn btn-primary" @click="searchMyChamp">
                검색
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import Header from "@/components/common/Header.vue";

const router = useRouter();
const word = ref("");

const searchMyChamp = function () {
  if (word.value.length < 1) {
    alert("소환사명#태그를 입력해 주세요");
  } else {
    const [summonerName, tagLine] = word.value.split("#");

    // 유효성 검사: 소환사 이름과 태그명이 모두 존재하는지 확인
    if (!summonerName || !tagLine) {
      alert("소환사명#태그를 올바르게 입력해 주세요. 예: 누가날막지#KR1");
      return; // 조기 반환을 통해 함수 실행 종료
    }

    router.push({
      name: "detail",
      query: { summonerName: summonerName, tagLine: tagLine },
    });
  }
};
</script>

<style scoped>
.hero {
  --default-color: #ffffff;
  --default-color-rgb: 255, 255, 255;
  --background-color: #000000;
  --background-color-rgb: 0, 0, 0;
  width: 100%;
  min-height: 100vh;
  position: relative;
  padding: 160px 0 80px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero img {
  position: absolute;
  inset: 0;
  display: block;
  width: 100%;
  height: 100%;
  -o-object-fit: cover;
  object-fit: cover;
  z-index: 1;
}

.hero:before {
  content: "";
  background: rgba(var(--background-color-rgb), 0.5);
  position: absolute;
  inset: 0;
  z-index: 2;
}

.hero .container {
  position: relative;
  margin-left: 150px;
  z-index: 3;
}

.hero h2 {
  color: white;
  margin: 0;
  font-size: 58px;
  font-weight: 700;
}

.hero p {
  color: white;
  margin: 5px 0 0 0;
  font-size: 25px;
}

.hero .btn {
  width: 66px;
  background-color: #005a82;
}

.hero .sign-up-form {
  width: 110%;
  height: 80px;
  margin-top: 20px;
  padding: 10px;
  border-radius: 7px;
  background: #fff;
  box-shadow: 0 15px 30px 0 rgba(0, 0, 0, 0.1);
}

.hero .sign-up-form .form-control {
  height: 100%;
  border: none;
}

.hero .sign-up-form .form-control:active,
.hero .sign-up-form .form-control:focus {
  outline: none;
  box-shadow: none;
}

.hero .sign-up-form input[type="submit"] {
  box-shadow: 0 15px 30px 0 rgba(0, 0, 0, 0.2);
  background-color: var(#87ceeb);
  border-color: var(#87ceeb);
  padding: 8px 20px 10px 20px;
  border-radius: 7px;
  color: var(--contrast-color);
}

@media (max-width: 768px) {
  .hero h2 {
    font-size: 32px;
  }

  .hero p {
    font-size: 18px;
  }
}
</style>
