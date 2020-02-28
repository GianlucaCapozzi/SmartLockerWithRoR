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
        if user
            temp_pass = SecureRandom.base64.to_s
            
            user.temp_pass = temp_pass
            user.reset_pass = true
            user.save

            UserMailer.recovery_password(user, temp_pass).deliver
        else
            errors.add(:user, 'User not found')
        end
    end

end