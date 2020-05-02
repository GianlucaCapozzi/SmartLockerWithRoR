class AuthOauthUser
    prepend SimpleCommand

    def initialize(email, token_oauth)
        @email = email
        @token_oauth = token_oauth
    end
  
    def call
        res = check(get_info_token_oauth)
        if res == 'OK'
            return get_id
        elsif res == 'Not completed'
            return get_configure_token
        end
        nil
    end

    private
  
    def check(id_oauth)
        user = User.find_by(id_oauth: id_oauth)
        if user and user.oauth and user.email==@email
            if not (user.email_confirmed and user.info_completed)
                create_conf_token(user)
                errors.add(:user_authentication, 'User not completed')
                return 'Not completed'
            else
                return 'OK'
            end
        else
            errors.add(:user_authentication, 'Token not valid')
            return 'Token not valid'
        end
    end

    def get_info_token_oauth
        response = HTTParty.get('https://graph.facebook.com/debug_token?input_token='+@token_oauth+'&access_token='+ENV["APP_ID_FACEBOOK"]+'|'+ENV["SECRET_KEY_FACEBOOK"])
        if response.code == 200
            body = JSON.parse(response.body)
            if body['data']['is_valid']
                return body['data']['user_id']
            else
                errors.add(:token, 'Token not valid')
            end
        else
            errors.add(:token, 'Token not valid')
        end
        nil
    end

    def create_conf_token(user)
        if user.configure_token.nil?
            user.configure_token = SecureRandom.urlsafe_base64.to_s
            user.save
        end
    end

    def get_id
        User.find_by_email(@email)['id']
    end

    def get_configure_token
        User.find_by_email(@email)['configure_token']
    end
end