class RegisterUserOauth
    prepend SimpleCommand

    def initialize(email, token_oauth)
        @email = email
        @token_oauth = token_oauth
    end

    def call
        if signup_check
            @user = User.create(email: @email, password: get_id_oauth, password_confirmation: get_id_oauth)
            @user = User.find_by_email(@email)
            email_activate(@user)
            UserMailer.registration_oauth(@user).deliver
            JsonWebToken::encode(user_id: @user.id)
        end
    end

    private

    attr_accessor :email, :token_oauth

    def signup_check
        exist_email = User.exists?(email: @email)

        if exist_email
            errors.add(:user_registration, 'Email already used')
            return false
        else
            return true
        end
        
    end

    def email_activate(user)
        user.email_confirmed = true
        user.confirm_token = nil
        user.oauth = true
        user.id_oauth = get_id_oauth
        user.save
    end

    def get_id_oauth
        response = HTTParty.get('https://graph.facebook.com/debug_token?input_token='+@token_oauth+'&access_token='+ENV["APP_ID_FACEBOOK"]+'|'+ENV["SECRET_KEY_FACEBOOK"])
        if response.code == 200
            body = JSON.parse(response.body)
            return body['data']['user_id'] if body['data']['is_valid']

            errors.add(:token, 'Token not valid')
        else
            errors.add(:token, 'Token not valid')
        end
        nil
    end
end