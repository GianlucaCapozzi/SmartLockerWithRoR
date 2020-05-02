class AuthenticateUser
    prepend SimpleCommand
  
    def initialize(email, password)
        @email = email
        @password = password
    end
  
    def call
        res = check
        if res == 'OK'
            return JsonWebToken::encode(user_id: get_id)
        elsif res == 'Not completed'
            return get_configure_token
        end
        nil
    end
  
    private
  
    attr_accessor :email, :password
  
    def check
        user = User.find_by_email(@email)
        if user and (not user.oauth) and user.authenticate(password)
            if not (user.email_confirmed and user.info_completed)
                create_conf_token(user)
                errors.add(:user_authentication, 'User not completed')
                return 'Not completed'
            else
                return 'OK'
            end
        else
            errors.add(:user_authentication, 'Invalid Credentials')
            return 'Invalid Credentials'
        end
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