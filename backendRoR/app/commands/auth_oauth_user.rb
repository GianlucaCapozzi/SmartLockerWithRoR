class AuthOauthUser
    prepend SimpleCommand

    def initialize(email, token_oauth)
        @email = email
        @token_oauth = token_oauth
    end
  
    def call
        check = user(get_info_token_oauth)
        check if check
    end

    private
  
    def user(id_oauth)
        user = User.find_by(id_oauth: id_oauth)
        return user if user && user.oauth && user.email==@email && user.email_confirmed && user.info_completed

        errors.add(:user_authentication, 'Token not valid')
        nil
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
end