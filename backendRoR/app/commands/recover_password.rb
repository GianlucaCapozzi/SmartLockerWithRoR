class RecoverPassword
    prepend SimpleCommand

    def initialize(email)
        @email = email
    end

    def call 
        recover
    end

    private
    def recover
        user = User.find_by_email(@email)
        if user and not user.oauth
            temp_pass = SecureRandom.base64.to_s
            
            user.temp_pass = temp_pass
            user.reset_pass = true
            user.save

            UserMailer.recovery_password(user, temp_pass).deliver
        elsif user.oauth
            errors.add(:user, 'User created with oAuth')
        else
            errors.add(:user, 'User not found')
        end
    end

end