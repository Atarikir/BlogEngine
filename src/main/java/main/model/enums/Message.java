package main.model.enums;

public enum Message {
    ERROR_EMAIL("Этот e-mail уже зарегистрирован"),
    ERROR_NAME("Имя указано неверно"),
    ERROR_PASSWORD("Пароль короче 6-ти символов"),
    ERROR_CAPTCHA("Код с картинки введён неверно"),
    ERROR_PHOTO("Фото слишком большое, нужно не более 5 Мб"),
    ERROR_EXTENSION("Файл не формата изображение jpg"),
    ERROR_CODE("Ссылка для восстановления пароля устарела." +
            "<a href=\"/login/restore-password\">Запросить ссылку снова</a>");

    private String text;

    Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
