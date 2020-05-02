class RegisterUser
    prepend SimpleCommand

    def initialize(email, password)
        @email = email
        @password = password
    end

    def call
        if signup_check
            @user = User.create(email: @email , password: @password , password_confirmation: @password) 
            @user = User.find_by_email(@email)
            create_token(@user)
            token = @user.configure_token
            BlacklistedToken.create(token: token, user: @user, expire_at: 24.hours.from_now.to_i)
            UserMailer.registration_confirmation(@user).deliver
            return token
        end
    end

    private

    attr_accessor :email, :password

    def signup_check
        exist_email = User.exists?(email: @email)

        if exist_email
            errors.add(:user_registration, 'Email already used')
            return false
        else
            return true
        end
        
    end

    def create_token(user)
        if user.confirm_token.nil? or user.configure_token.nil?
            user.confirm_token = SecureRandom.urlsafe_base64.to_s
            user.configure_token = SecureRandom.urlsafe_base64.to_s
            user.save
        end
    end
end